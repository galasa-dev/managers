package dev.galasa.zosfile.zosmf.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosFileManagerField;
import dev.galasa.zosfile.zosmf.manager.internal.properties.ZosFileZosmfPropertiesSingleton;
import dev.galasa.zosmf.spi.IZosmfManagerSpi;

/**
 * zOS File Manager implemented using zOS/MF
 *
 */
@Component(service = { IManager.class })
public class ZosFileManagerImpl extends AbstractManager {
	protected static final String NAMESPACE = "zosfile";

	protected static IZosManagerSpi zosManager;
	public static void setZosManager(IZosManagerSpi zosManager) {
		ZosFileManagerImpl.zosManager = zosManager;
	}
	
	protected static IZosmfManagerSpi zosmfManager;
	public static void setZosmfManager(IZosmfManagerSpi zosmfManager) {
		ZosFileManagerImpl.zosmfManager = zosmfManager;
	}

	private final List<ZosFileHandlerImpl> zosFileHandlers = new ArrayList<>();
	
	protected static Path archivePath;
	public static void setArchivePath(Path archivePath) {
		ZosFileManagerImpl.archivePath = archivePath;
	}
	
	protected static Method currentTestMethod;
	public static void setCurrentTestMethod(Method testMethod) {
		ZosFileManagerImpl.currentTestMethod = testMethod;
	}
	
	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);
		try {
			ZosFileZosmfPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosFileManagerException("Unable to request framework services", e);
		}

		//*** Check to see if any of our annotations are present in the test class
		//*** If there is,  we need to activate
		List<AnnotatedField> ourFields = findAnnotatedFields(ZosFileManagerField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
		}
		
		Path artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
    	setArchivePath(artifactsRoot.resolve("zosDatasets"));
	}
	

	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
	 */
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(ZosFileManagerField.class);
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
			throw new ZosFileManagerException("The zOS Manager is not available");
		}
		setZosmfManager(addDependentManager(allManagers, activeManagers, IZosmfManagerSpi.class));
		if (zosmfManager == null) {
			throw new ZosFileManagerException("The zOSMF Manager is not available");
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
    	Iterator<ZosFileHandlerImpl> zosFileHandlerImplIterator = this.zosFileHandlers.iterator();
    	while (zosFileHandlerImplIterator.hasNext()) {
    		zosFileHandlerImplIterator.next().cleanup();
		}

    	setCurrentTestMethod(null);
    	
        return null;
    }
	
    @GenerateAnnotatedField(annotation=ZosFileHandler.class)
	public IZosFileHandler generateZosFile(Field field, List<Annotation> annotations) {
    	ZosFileHandlerImpl zosFileHandlerImpl = new ZosFileHandlerImpl(field.getName());
    	this.zosFileHandlers.add(zosFileHandlerImpl);    	
		return zosFileHandlerImpl;
	}
}
