/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
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
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
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

	private static final List<ZosFileHandlerImpl> zosFileHandlers = new ArrayList<>();

	private static String runId;
	protected static void setRunId(String id) {
		runId = id;
	}
	protected static String getRunId() {
		return runId;
	}

	private static Path datasetArtifactRoot;
	protected static void setDatasetArtifactRoot(Path path) {
		datasetArtifactRoot = path;
	}
	protected static Path getDatasetArtifactRoot() {
		return datasetArtifactRoot;
	}

	private static Path vsamDatasetArtifactRoot;
	protected static void setVsamDatasetArtifactRoot(Path path) {
		vsamDatasetArtifactRoot = path;
	}
	protected static Path getVsamDatasetArtifactRoot() {
		return vsamDatasetArtifactRoot;
	}

	private static Path unixPathArtifactRoot;
	protected static void setUnixPathArtifactRoot(Path path) {
		unixPathArtifactRoot = path;
	}
	protected static Path getUnixPathArtifactRoot() {
		return unixPathArtifactRoot;
	}
	
	protected static String currentTestMethod;
	public static void setCurrentTestMethod(String testMethod) {
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
		
		setRunId(getFramework().getTestRunName());
		
		setDatasetArtifactRoot(getFramework().getResultArchiveStore().getStoredArtifactsRoot().resolve("zOS_Datasets"));		
		setVsamDatasetArtifactRoot(getFramework().getResultArchiveStore().getStoredArtifactsRoot().resolve("zOS_VSAM_Datasets"));		
		setUnixPathArtifactRoot(getFramework().getResultArchiveStore().getStoredArtifactsRoot().resolve("zOS_Unix_Paths"));
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
    	setCurrentTestMethod(testMethod.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestMethod(java.lang.reflect.Method,java.lang.String,java.lang.Throwable)
     */
    @Override
    public String endOfTestMethod(@NotNull Method testMethod, @NotNull String currentResult, Throwable currentException) throws ManagerException {
    	Iterator<ZosFileHandlerImpl> zosFileHandlerImplIterator = zosFileHandlers.iterator();
    	while (zosFileHandlerImplIterator.hasNext()) {
    		zosFileHandlerImplIterator.next().cleanupEndOfTestMethod();
		}

    	setCurrentTestMethod(null);
    	
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestClass(java.lang.String,java.lang.Throwable)
     */
    @Override
    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
    	setCurrentTestMethod("endOfTestCleanup");
    	Iterator<ZosFileHandlerImpl> zosFileHandlerImplIterator = zosFileHandlers.iterator();
    	while (zosFileHandlerImplIterator.hasNext()) {
    		zosFileHandlerImplIterator.next().cleanupEndOfClass();
		}
    	
        return null;
    }
	
    @GenerateAnnotatedField(annotation=ZosFileHandler.class)
	public IZosFileHandler generateZosFile(Field field, List<Annotation> annotations) {
    	ZosFileHandlerImpl zosFileHandlerImpl = new ZosFileHandlerImpl(field.getName());
    	zosFileHandlers.add(zosFileHandlerImpl);    	
		return zosFileHandlerImpl;
	}
    
    public static IZosFileHandler newZosFileHandler() {
    	ZosFileHandlerImpl zosFileHandlerImpl = new ZosFileHandlerImpl();
    	zosFileHandlers.add(zosFileHandlerImpl);    	
		return zosFileHandlerImpl;
	}
    
	public static String getRunDatasetHLQ(IZosImage image) throws ZosFileManagerException {
		try {
			return zosManager.getRunDatasetHLQ(image);
		} catch (ZosManagerException e) {
			throw new ZosFileManagerException(e);
		}
	}
}
