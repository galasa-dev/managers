/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.resource.internal;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.cicsts.spi.ICicsResourceProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.spi.IZosLibertySpi;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;

@Component(service = { IManager.class })
public class CicsResourceManagerImpl extends AbstractManager implements ICicsResourceProvider {
    
    private static final Log logger = LogFactory.getLog(CicsResourceManagerImpl.class);
    
    protected static final String NAMESPACE = "cicsresource";
    private ICicstsManagerSpi cicstsManager;    
    private IZosBatchSpi zosBatchManager;    
    private IZosFileSpi zosFileManager;
	private IZosLibertySpi zosLibertyManager;
    protected IZosUNIXCommandSpi zosUnixCommandManager;
	private ITextScannerManagerSpi textScannerManager;

    private static final String JVMSERVERS = "JVMServers";

    private static final String PROVISIONING = "provisioning";
	
	private HashMap<ICicsRegion, ICicsResource> regionCicsResources = new HashMap<>();

    private Path artifactsRoot;
    public Path getArtifactsRoot() {
    	return artifactsRoot;
    }
    
    private Path archivePath;
    public Path getArchivePath() {
        return this.archivePath;
    }
    
    private String currentTestMethodArchiveFolderName;
    public Path getCurrentTestMethodArchiveFolder() {
        return archivePath.resolve(currentTestMethodArchiveFolderName);
    }
    
	/* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            youAreRequired(allManagers, activeManagers, galasaTest);
        }
        
        this.artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(JVMSERVERS);
        this.currentTestMethodArchiveFolderName = "preTest";
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        this.cicstsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
        if(cicstsManager == null) {
           throw new CicstsManagerException("The CICS Manager is not available");
        }
        this.zosBatchManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (this.zosBatchManager == null) {
            throw new CicstsManagerException("The zOS Batch Manager is not available");
        }
        this.zosFileManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFileManager == null) {
            throw new CicstsManagerException("The zOS File Manager is not available");
        }
        this.zosLibertyManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosLibertySpi.class);
        if (this.zosLibertyManager == null) {
            throw new CicstsManagerException("The zOS Liberty Manager is not available");
        }
        this.zosUnixCommandManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosUNIXCommandSpi.class);
        if (this.zosUnixCommandManager == null) {
            throw new CicstsManagerException("The zOS UNIX Command Manager is not available");
        }
        this.textScannerManager = addDependentManager(allManagers, activeManagers, galasaTest, ITextScannerManagerSpi.class);
        if (this.textScannerManager == null) {
            throw new CicstsManagerException("The Text Scan Manager is not available");
        }
        cicstsManager.registerCicsResourceProvider(this);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
        cleanup(false);
        this.archivePath = artifactsRoot.resolve(JVMSERVERS);
        if (galasaMethod.getJavaTestMethod() != null) {
        	this.currentTestMethodArchiveFolderName = galasaMethod.getJavaTestMethod().getName() + "." + galasaMethod.getJavaExecutionMethod().getName();
        } else {
        	this.currentTestMethodArchiveFolderName = galasaMethod.getJavaExecutionMethod().getName();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestMethod(java.lang.String,java.lang.Throwable)
     */
    @Override
    public String endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull String currentResult, Throwable currentException) throws ManagerException {
        cleanup(false);
        
        return null;
    }

    /* (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestClass(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(JVMSERVERS);
        this.currentTestMethodArchiveFolderName = "postTest";
        cleanup(false);
        
        return null;
    }

    /* (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestRun()
     */
    @Override
    public void endOfTestRun() {
        try {
            cleanup(true);
        } catch (ZosBatchException e) {
            logger.error("Problem in endOfTestRun()", e);
        }
    }
    
    protected void cleanup(boolean endOfTest) throws ZosBatchException {
    	//TODO
//        for (Entry<String, JvmserverImpl> entry : this.jvmServers.entrySet()) {
//            entry.getValue().cleanup(endOfTest);
//        }
    }

	@Override
	public @NotNull ICicsResource getCicsResource(ICicsRegion cicsRegion) throws CicsResourceManagerException {
		ICicsResource cicsResources = this.regionCicsResources.get(cicsRegion);
        if (cicsResources == null) {
            cicsResources = new CicsResourceImpl(this, cicsRegion);
            this.regionCicsResources.put(cicsRegion, cicsResources);
        }
        return cicsResources;
	}
	
	protected ICicstsManagerSpi getCicsManager() {
		return this.cicstsManager;
	}
	
	protected IZosFileHandler getZosFileHandler() throws CicsResourceManagerException {
		try {
			return this.zosFileManager.getZosFileHandler();
		} catch (ZosFileManagerException e) {
			throw new CicsResourceManagerException("Problem getting IZosFileHandler", e);
		}
	}
	
	protected IZosBatch getBatch(IZosImage zosImage) {
		return this.zosBatchManager.getZosBatch(zosImage);
	}
	
	protected IZosLiberty getZosLiberty() throws CicsResourceManagerException {
		try {
			return this.zosLibertyManager.getZosLiberty();
		} catch (ZosLibertyManagerException e) {
			throw new CicsResourceManagerException("Problem getting IZosLiberty", e);
		}
	}
	
	protected @NotNull IZosUNIXCommand getZosUnixCommand(IZosImage image) {
		return this.zosUnixCommandManager.getZosUNIXCommand(image);
	}

	protected ILogScanner getLogScanner() throws CicsResourceManagerException {
		try {
			return this.textScannerManager.getLogScanner();
		} catch (TextScanManagerException e) {
			throw new CicsResourceManagerException("Problem getting ILogScanner", e);
		}
	}
}
