package io.ejat.zos.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.AbstractManager;
import io.ejat.framework.spi.AnnotatedField;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.GenerateAnnotatedField;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IManager;
import io.ejat.framework.spi.ManagerException;
import io.ejat.framework.spi.ResourceUnavailableException;
import io.ejat.ipnetwork.IIpHost;
import io.ejat.ipnetwork.IIpPort;
import io.ejat.ipnetwork.spi.IIpNetworkManagerSpi;
import io.ejat.zos.IZosImage;
import io.ejat.zos.IZosManager;
import io.ejat.zos.ZosImage;
import io.ejat.zos.ZosIpHost;
import io.ejat.zos.ZosIpPort;
import io.ejat.zos.ZosManagerException;

@Component(service = { IManager.class })
public class ZosManagerImpl extends AbstractManager implements IZosManager {
	protected final static String NAMESPACE = "zos";

	private final static Log logger = LogFactory.getLog(ZosManagerImpl.class);

	private ZosProperties zosProperties;
	private IConfigurationPropertyStoreService cps;
	private IDynamicStatusStoreService dss;
	private IIpNetworkManagerSpi ipManager;

	private final HashMap<String, ZosImageImpl> taggedImages = new HashMap<>();
	private final HashMap<String, ZosImageImpl> images = new HashMap<>();

	/* 
	 * By default we need to load the zosmf batch implementation, but provide the ability for 
	 * it to be overridden
	 */
	@Override
	public List<String> extraBundles(@NotNull IFramework framework) throws ZosManagerException {
		this.zosProperties = new ZosProperties(framework);

		ArrayList<String> bundles = new ArrayList<>();
		String batchBundleName = this.zosProperties.getBatchExtraBundle();
		if (batchBundleName == null)  {
			batchBundleName = "io.ejat.zosbatch.zosmf.manager";
		}

		bundles.add(batchBundleName);

		return bundles;
	}

	/* (non-Javadoc)
	 * @see io.ejat.framework.spi.AbstractManager#initialise(io.ejat.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);

		//*** Check to see if any of our annotations are present in the test class
		//*** If there is,  we need to activate
		List<AnnotatedField> ourFields = findAnnotatedFields(ZosManagerField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
		}

		try {
			this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
			this.cps = framework.getConfigurationPropertyService(NAMESPACE);
		} catch (Exception e) {
			throw new ZosManagerException("Unable to request framework services", e);
		}
	}


	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
		ipManager = addDependentManager(allManagers, activeManagers, IIpNetworkManagerSpi.class);
		if (ipManager == null) {
			throw new ZosManagerException("The IP Network Manager is not available");
		}
	}

	/* (non-Javadoc)
	 * @see io.ejat.framework.spi.AbstractManager#provisionGenerate()
	 */
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		//*** Get all our annotated fields
		List<AnnotatedField> annotatedFields = findAnnotatedFields(ZosManagerField.class);

		//*** First, locate the IZosImpl field that has a tag of primary
		//*** And then generate it
		Iterator<AnnotatedField> annotatedFieldIterator = annotatedFields.iterator();
		while(annotatedFieldIterator.hasNext()) {
			AnnotatedField annotatedField = annotatedFieldIterator.next();
			final Field field = annotatedField.getField();
			final List<Annotation> annotations = annotatedField.getAnnotations();

			if (field.getType() == IZosImage.class) {
				ZosImage annotationZosImage = field.getAnnotation(ZosImage.class);
				String tag = annotationZosImage.imageTag();
				if (tag == null || "primary".equals(tag.toLowerCase())) {
					IZosImage zosImage = generateZosImage(field, annotations);
					registerAnnotatedField(field, zosImage);
					annotatedFieldIterator.remove(); //*** Dont need it for the second pass
					break;
				}
			}
		}

		//*** Second pass, generate all the remaining zosimages now the primary is allocated
		for(AnnotatedField annotatedField : annotatedFields) {
			final Field field = annotatedField.getField();
			final List<Annotation> annotations = annotatedField.getAnnotations();

			//*** Check this field has not been annotated already
			if (getAnnotatedField(field) != null) {
				continue;
			}

			if (field.getType() == IZosImage.class) {
				IZosImage zosImage = generateZosImage(field, annotations);
				registerAnnotatedField(field, zosImage);
			}
		}

		//*** Auto generate the remaining fields
		generateAnnotatedFields(ZosManagerField.class);
	}



	/* (non-Javadoc)
	 * @see io.ejat.framework.spi.AbstractManager#provisionDiscard()
	 */
	@Override
	public void provisionDiscard() {
		//*** Free up any slots we have allocated for this run;

		for(ZosImageImpl image : images.values()) {
			image.freeImage();
		}
	}


	//*** We do not allow auto generate of the zos image fields as they need
	//*** to be done first AND the primary image needs to be the first one
	private IZosImage generateZosImage(Field field, List<Annotation> annotations) throws ZosManagerException {
		ZosImage annotationZosImage = field.getAnnotation(ZosImage.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationZosImage.imageTag(), "primary");

		//*** Have we already generated this tag
		if (taggedImages.containsKey(tag)) {
			return taggedImages.get(tag);
		}

		//*** See if the image ID is already set for this tag
		String imageID = zosProperties.getImageIdForTag(tag);
		if (imageID != null) {
			//*** Do we already have it
			if (images.containsKey(imageID)) {
				ZosImageImpl selectedImage = images.get(imageID);
				taggedImages.put(tag, selectedImage);
				return selectedImage;
			}

			ZosImageImpl image = new ZosImageImpl(this, imageID, null);
			if (image.allocateImage()) {
				logger.info("zOS Image " + image.getImageID() + " selected for zosTag '" + tag + "'");
				return image;
			} else {
				throw new ZosManagerException("Unable to provision zOS Image tagged " + tag + " on " + imageID + " as there is insufficient capacity");
			}
		} 

		return selectNewImage(tag);
	}

	@GenerateAnnotatedField(annotation=ZosIpHost.class)
	public IIpHost generateIpHost(Field field, List<Annotation> annotations) throws ZosManagerException {
		ZosIpHost annotationHost = field.getAnnotation(ZosIpHost.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationHost.imageTag(), "primary");

		//*** Ensure we have this tagged host
		ZosImageImpl image = taggedImages.get(tag);
		if (image == null) { 
			throw new ZosManagerException("Unable to provision an IP Host for field " + field.getName() + " as no @ZosImage for the tag '" + tag + "' was present");
		}

		return image.getIpHost();
	}

	@GenerateAnnotatedField(annotation=ZosIpPort.class)
	public IIpPort generateIpPort(Field field, List<Annotation> annotations) throws ZosManagerException {
		ZosIpPort annotationPort = field.getAnnotation(ZosIpPort.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationPort.imageTag(), "primary");
		String type = defaultString(annotationPort.type(), "standard");

		//*** Ensure we have this tagged host
		ZosImageImpl image = taggedImages.get(tag);
		if (image == null) { 
			throw new ZosManagerException("Unable to provision an IP Host for field " + field.getName() + " as no @ZosImage for the tag '" + tag + "' was present");
		}

		try {
			return image.getIpHost().provisionPort(type);
		} catch(Exception e) {
			throw new ZosManagerException("Unable to provision a port for zOS Image " + image.getImageID() + ", type=" + type, e);
		}
	}

	protected ZosImageImpl selectNewImage(String tag) throws ZosManagerException {
		//***  Need the cluster we can allocate an image from
		String clusterId = zosProperties.getClusterIdForTag(tag);
		if (clusterId == null) {
			clusterId = "default";
		}

		//*** Find a list of images
		List<ImageUsage> definedImages = new ArrayList<>();
		for(String definedImage : zosProperties.getClusterImages(clusterId)) {
			ZosImageImpl image = new ZosImageImpl(this, definedImage, clusterId);
			definedImages.add(new ImageUsage(image));
		}

		Collections.sort(definedImages);

		//*** First attempt to use an image that has not been selected for this test yet
		for(ImageUsage image : definedImages) {
			if (this.images.containsKey(image.image.getImageID())) {
				continue;
			}

			if (image.image.allocateImage()) {
				logger.info("zOS Image " + image.image.getImageID() + " selected for zosTag '" + tag + "' with slot name " + image.image.getSlotName());
				taggedImages.put(tag, image.image);
				images.put(image.image.getImageID(), image.image);

				return image.image;
			}
		}

		//*** Can do some other stuff in the future to reuse already allocated lpars,  but not for now

		throw new ZosManagerException("Insufficent capacity for images in cluster " + clusterId);
	}


	protected IDynamicStatusStoreService getDSS() {
		return this.dss;
	}

	public IConfigurationPropertyStoreService getCPS() {
		return this.cps;
	}
	public ZosProperties getZosProperties() {
		return this.zosProperties;
	}


	private static class ImageUsage implements Comparable<ImageUsage> {
		private final ZosImageImpl image;
		private       Float        usage;

		public ImageUsage(ZosImageImpl image) throws ZosManagerException {
			this.image = image;
			usage = image.getCurrentUsage();
		}

		@Override
		public int compareTo(ImageUsage o) {
			return usage.compareTo(o.usage);
		}
	}


	protected IIpNetworkManagerSpi getIpManager() {
		return this.ipManager;
	}




}
