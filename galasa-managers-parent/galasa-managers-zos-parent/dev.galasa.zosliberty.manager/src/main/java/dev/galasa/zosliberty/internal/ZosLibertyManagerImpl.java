/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.zosliberty.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.ZosLiberty;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.internal.properties.ZosLibertyPropertiesSingleton;
import dev.galasa.zosliberty.spi.IZosLibertySpi;

@Component(service = { IManager.class })
public class ZosLibertyManagerImpl extends AbstractManager implements IZosLibertySpi {

    private static final Log logger = LogFactory.getLog(ZosLibertyManagerImpl.class);

    protected static final String NAMESPACE = "zosliberty";

    private static final String LIBERTY_SERVERS = "LibertyServers";

    private static final String PROVISIONING = "provisioning";

    private IZosManagerSpi  zosManager;
	private IZosFileSpi zosFileManager;
	private ITextScannerManagerSpi textScannerManager;

//    private final HashMap<String, IZosLiberty> taggedZosLibertys = new HashMap<>();
//    private final HashMap<String, IZosLiberty> zosLibertys = new HashMap<>();

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
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosLibertyPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosLibertyManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosLibertyField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers,galasaTest);
            }
        }
        this.artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(LIBERTY_SERVERS);
        this.currentTestMethodArchiveFolderName = "preTest";
    }


    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new ZosLibertyManagerException("The zOS Manager is not available");
        }
        this.zosFileManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFileManager == null) {
            throw new ZosLibertyManagerException("The zOS File Manager is not available");
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
        return otherManager instanceof IZosManagerSpi;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
        cleanup(false);
        this.archivePath = artifactsRoot.resolve(LIBERTY_SERVERS);
        if (galasaMethod.getJavaTestMethod() != null) {
        	this.currentTestMethodArchiveFolderName = galasaMethod.getJavaTestMethod().getName() + "." + galasaMethod.getJavaExecutionMethod().getName();
        } else {
        	this.currentTestMethodArchiveFolderName = galasaMethod.getJavaExecutionMethod().getName();
        }
    }

    /* (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestRun()
     */
    @Override
    public void endOfTestRun() {
        try {
            cleanup(true);
        } catch (ZosLibertyManagerException e) {
            logger.error("Problem in endOfTestRun()", e);
        }
    }
    
    protected void cleanup(boolean endOfTest) throws ZosLibertyManagerException {
    	//TODO
//        for (Entry<String, JvmserverImpl> entry : this.jvmServers.entrySet()) {
//            entry.getValue().cleanup(endOfTest);
//        }
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosLibertyField.class);
    }

    @GenerateAnnotatedField(annotation=ZosLiberty.class)
    public IZosLiberty generateZosmf(Field field, List<Annotation> annotations) throws ZosLibertyManagerException {
//        ZosLiberty annotationZosmf = field.getAnnotation(ZosLiberty.class);
//
//        //*** Default the tag to primary
//        String tag = defaultString(annotationZosmf.imageTag(), "PRIMARY").toUpperCase();
//
//        //*** Have we already generated this tag
//        if (taggedZosLibertys.containsKey(tag)) {
//            return taggedZosLibertys.get(tag);
//        }
//
//        // TODO this needs to be proper DSEd
//        IZosImage zosImage = null;
//        try {
//            zosImage = getZosManager().getImageForTag(tag);
//        } catch(Exception e) {
//            throw new ZosLibertyManagerException("Unable to locate z/OS image for tag '" + tag + "'", e);
//        }
//
//        // TODO should be the DSE server id or provision one
//
//        Map<String, IZosLiberty> possibleZosmfs = getZosLibertys(zosImage);
//        if (possibleZosmfs.isEmpty()) {
//            throw new ZosLibertyManagerException("Unable to provision zOS/MF, no zOS/MF server defined for image tag '" + tag + "'");
//        }
//
//        IZosLiberty selected = possibleZosmfs.values().iterator().next();  // TODO do we want to randomise this?
//        taggedZosLibertys.put(tag, selected);

        return new ZosLibertyImpl(this);
    }


    public Map<String, IZosLiberty> getZosLibertys(@NotNull IZosImage zosImage) throws ZosLibertyManagerException {
        HashMap<String, IZosLiberty> possibleZosmfs = new HashMap<>();

//        try {
//            List<String> possibleServers = ImageServers.get(zosImage);
//            if (possibleServers.isEmpty()) {
//                possibleServers = SysplexServers.get(zosImage);
//                if (possibleServers.isEmpty()) {
//                    // Default to assume there is a zOS/MF server running on the same image on port 443
//                    possibleServers = new ArrayList<String>(1);
//                    possibleServers.add(zosImage.getImageID());
//                }
//            }
//
//
//            for (String serverId : possibleServers) {
//                IZosmf actualZosmf = this.zosLibertys.get(serverId);
//
//                if (actualZosmf == null) {
//                    logger.trace("Retreiving zOS server " + serverId);
//                    actualZosmf = newZosmf(serverId);
//                    this.zosLibertys.put(serverId, actualZosmf);
//                }
//                possibleZosmfs.put(serverId, actualZosmf);
//            }
//        } catch (ZosLibertyManagerException e) {
//            throw new ZosLibertyManagerException("Unable to get zOS Liberty server for image \"" + zosImage.getImageID() + "\"", e);
//        }
        return possibleZosmfs;
    }


    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }


	@Override
	public @NotNull IZosLiberty getZosLiberty() throws ZosLibertyManagerException {
		return new ZosLibertyImpl(this);
	}


	public IZosFileHandler getZosFileHandler() throws ZosLibertyManagerException {
		try {
			return this.zosFileManager.getZosFileHandler();
		} catch (ZosFileManagerException e) {
			throw new ZosLibertyManagerException("Problem getting IZosFileHandler", e);
		}
	}

	protected ILogScanner getLogScanner() throws ZosLibertyManagerException {
		try {
			return this.textScannerManager.getLogScanner();
		} catch (TextScanManagerException e) {
			throw new ZosLibertyManagerException("Problem getting ILogScanner", e);
		}
	}
}
