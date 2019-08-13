package dev.voras.common.zosmf.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.voras.ManagerException;
import dev.voras.common.http.spi.IHttpManagerSpi;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zos.ZosManagerException;
import dev.voras.common.zos.spi.IZosManagerSpi;
import dev.voras.common.zosmf.IZosmf;
import dev.voras.common.zosmf.Zosmf;
import dev.voras.common.zosmf.ZosmfException;
import dev.voras.common.zosmf.ZosmfManagerException;
import dev.voras.common.zosmf.internal.properties.ServerImages;
import dev.voras.common.zosmf.internal.properties.ZosmfPropertiesSingleton;
import dev.voras.common.zosmf.spi.IZosmfManagerSpi;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.AnnotatedField;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.GenerateAnnotatedField;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IManager;
import dev.voras.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class ZosmfManagerImpl extends AbstractManager implements IZosmfManagerSpi {
	
	private static final Log logger = LogFactory.getLog(ZosmfManagerImpl.class);
	
	protected static final String NAMESPACE = "zosmf";
	
	protected static IZosManagerSpi zosManager;
	public static void setZosManager(IZosManagerSpi zosManager) {
		ZosmfManagerImpl.zosManager = zosManager;
	}

	protected static IHttpManagerSpi httpManager;	
	public static void setHttpManager(IHttpManagerSpi httpManager) {
		ZosmfManagerImpl.httpManager = httpManager;
	}

	private final HashMap<String, IZosmf> taggedZosmfs = new HashMap<>();
	private final HashMap<String, IZosmf> zosmfs = new HashMap<>();
	
	/* (non-Javadoc)
	 * @see dev.voras.framework.spi.AbstractManager#initialise(dev.voras.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);
		try {
			ZosmfPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosmfManagerException("Unable to request framework services", e);
		}

		//*** Check to see if any of our annotations are present in the test class
		//*** If there is,  we need to activate
		List<AnnotatedField> ourFields = findAnnotatedFields(ZosmfManagerField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
		}
	}
	

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
		setZosManager(addDependentManager(allManagers, activeManagers, IZosManagerSpi.class));
		if (zosManager == null) {
			throw new ZosManagerException("The zOS Manager is not available");
		}
		setHttpManager(addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class));
		if (httpManager == null) {
			throw new ZosManagerException("The HTTP Manager is not available");
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework
     * .spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
    	return otherManager instanceof IZosManagerSpi ||
    		   otherManager instanceof IHttpManagerSpi;
    }
	
	
	/* (non-Javadoc)
	 * @see dev.voras.framework.spi.AbstractManager#provisionGenerate()
	 */
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(ZosmfManagerField.class);
	}
	
	@GenerateAnnotatedField(annotation=Zosmf.class)
	public IZosmf generateZosmf(Field field, List<Annotation> annotations) throws ZosmfManagerException {
		Zosmf annotationZosmf = field.getAnnotation(Zosmf.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationZosmf.imageTag(), "primary");

		//*** Have we already generated this tag
		if (taggedZosmfs.containsKey(tag)) {
			return taggedZosmfs.get(tag);
		}

		ZosmfImpl zosmf = new ZosmfImpl(tag);
		taggedZosmfs.put(tag, zosmf);
		zosmfs.put(zosmf.getImage().getImageID(), zosmf);
		
		return zosmf;
	}


	@Override
	public IZosmf newZosmf(IZosImage image) throws ZosmfException {
		return new ZosmfImpl(image);
	}


	@Override
	public HashMap<String, IZosmf> getZosmfs(@NotNull String clusterId) throws ZosmfManagerException {
		try {
			for (String imageId : ServerImages.get(clusterId)) {
				logger.info("Requesting zOS image " + imageId + " for zOSMF server");
				IZosImage zosmfImage = zosManager.getImage(imageId);
				this.zosmfs.put(zosmfImage.getImageID(), newZosmf(zosmfImage));
			}
		} catch (ZosManagerException e) {
			throw new ZosmfManagerException("Unable to get zOSMF servers for cluster " + clusterId, e);
		}
		return zosmfs;
	}
}
