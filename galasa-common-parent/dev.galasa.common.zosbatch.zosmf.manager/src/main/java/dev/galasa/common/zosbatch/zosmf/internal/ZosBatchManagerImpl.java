package dev.galasa.common.zosbatch.zosmf.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.common.http.spi.IHttpManagerSpi;
import dev.galasa.common.zos.IZosImage;
import dev.galasa.common.zos.ZosManagerException;
import dev.galasa.common.zos.spi.IZosManagerSpi;
import dev.galasa.common.zosbatch.IZosBatch;
import dev.galasa.common.zosbatch.IZosBatchJobname;
import dev.galasa.common.zosbatch.ZosBatch;
import dev.galasa.common.zosbatch.ZosBatchField;
import dev.galasa.common.zosbatch.ZosBatchJobname;
import dev.galasa.common.zosbatch.ZosBatchManagerException;
import dev.galasa.common.zosbatch.zosmf.internal.properties.ZosBatchZosmfPropertiesSingleton;
import dev.galasa.common.zosmf.spi.IZosmfManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class ZosBatchManagerImpl extends AbstractManager {
	protected static final String NAMESPACE = "zosbatch";

	protected static IZosManagerSpi zosManager;
	public static void setZosManager(IZosManagerSpi zosManager) {
		ZosBatchManagerImpl.zosManager = zosManager;
	}
	
	protected static IZosmfManagerSpi zosmfManager;
	public static void setZosmfManager(IZosmfManagerSpi zosmfManager) {
		ZosBatchManagerImpl.zosmfManager = zosmfManager;
	}

	protected static IHttpManagerSpi httpManager;	
	public static void setHttpManager(IHttpManagerSpi httpManager) {
		ZosBatchManagerImpl.httpManager = httpManager;
	}

	private final HashMap<String, ZosBatchImpl> taggedZosBatches = new HashMap<>();
	
	protected static Path archivePath;
	public static void setArchivePath(Path archivePath) {
		ZosBatchManagerImpl.archivePath = archivePath;
	}
	
	protected static Method currentTestMethod;
	public static void setCurrentTestMethod(Method testMethod) {
		ZosBatchManagerImpl.currentTestMethod = testMethod;
	}
	
	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);
		try {
			ZosBatchZosmfPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosBatchManagerException("Unable to request framework services", e);
		}

		//*** Check to see if any of our annotations are present in the test class
		//*** If there is,  we need to activate
		List<AnnotatedField> ourFields = findAnnotatedFields(ZosBatchField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
		}
		
		Path artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
    	setArchivePath(artifactsRoot.resolve("zosBatchJobs"));
	}
	

	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
	 */
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(ZosBatchField.class);
	}


	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
	 */
	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
		setZosManager(addDependentManager(allManagers, activeManagers, IZosManagerSpi.class));
		if (zosManager == null) {
			throw new ZosBatchManagerException("The zOS Manager is not available");
		}
		setZosmfManager(addDependentManager(allManagers, activeManagers, IZosmfManagerSpi.class));
		if (zosmfManager == null) {
			throw new ZosBatchManagerException("The zOSMF Manager is not available");
		}
		setHttpManager(addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class));
		if (httpManager == null) {
			throw new ZosBatchManagerException("The HTTP Manager is not available");
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
    	return otherManager instanceof IZosManagerSpi ||
    		   otherManager instanceof IZosmfManagerSpi ||
    		   otherManager instanceof IHttpManagerSpi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull Method testMethod) throws ManagerException {
    	setCurrentTestMethod(testMethod);
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestMethod(java.lang.String,java.lang.Throwable)
     */
    @Override
    public String endOfTestMethod(@NotNull Method testMethod, @NotNull String currentResult, Throwable currentException) throws ManagerException {
    	for (HashMap.Entry<String, ZosBatchImpl> entry : this.taggedZosBatches.entrySet()) {
    		entry.getValue().cleanup();
		}

    	setCurrentTestMethod(null);
    	
        return null;
    }
	
    @GenerateAnnotatedField(annotation=ZosBatch.class)
	public IZosBatch generateZosBatch(Field field, List<Annotation> annotations) {
		ZosBatch annotationZosBatch = field.getAnnotation(ZosBatch.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationZosBatch.imageTag(), "primary");

		//*** Have we already generated this tag
		if (this.taggedZosBatches.containsKey(tag)) {
			return this.taggedZosBatches.get(tag);
		}

		IZosBatch zosBatch = new ZosBatchImpl();
		this.taggedZosBatches.put(tag, (ZosBatchImpl) zosBatch);
		
		return zosBatch;
	}
	
    @GenerateAnnotatedField(annotation=ZosBatchJobname.class)
	public IZosBatchJobname generateZosBatchJobname(Field field, List<Annotation> annotations) throws ZosBatchManagerException {
		ZosBatchJobname annotationZosBatchJobname = field.getAnnotation(ZosBatchJobname.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationZosBatchJobname.imageTag(), "primary");
		String imageid;
		try {
			IZosImage image = zosManager.getImageForTag(tag);
			imageid = image.getImageID();
		} catch (ZosManagerException e) {
			throw new ZosBatchManagerException("Unable to get image for tag \"" + tag + "\"", e);
		}
		return new ZosBatchJobnameImpl(imageid);
	}
}
