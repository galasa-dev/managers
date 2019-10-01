package dev.galasa.zosmf.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.Zosmf;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.properties.ServerImages;
import dev.galasa.zosmf.internal.properties.ZosmfPropertiesSingleton;
import dev.galasa.zosmf.spi.IZosmfManagerSpi;

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
	 * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
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
     * dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
    	return otherManager instanceof IZosManagerSpi ||
    		   otherManager instanceof IHttpManagerSpi;
    }
	
	
	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
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
