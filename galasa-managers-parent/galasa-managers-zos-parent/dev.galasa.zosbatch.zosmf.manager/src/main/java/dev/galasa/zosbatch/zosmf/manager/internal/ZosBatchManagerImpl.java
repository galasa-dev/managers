/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchField;
import dev.galasa.zosbatch.ZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.ZosBatchZosmfPropertiesSingleton;
import dev.galasa.zosmf.spi.IZosmfManagerSpi;

/**
 * zOS Batch Manager implemented using zOS/MF
 *
 */
@Component(service = { IManager.class })
public class ZosBatchManagerImpl extends AbstractManager implements IZosBatchSpi {
    protected static final String NAMESPACE = "zosbatch";
    
    private static final Log logger = LogFactory.getLog(ZosBatchManagerImpl.class);

    private static final String ZOSBATCH_JOBS = "zosBatchJobs";

    private static final String PROVISIONING = "provisioning";

    protected static IZosManagerSpi zosManager;
    public static void setZosManager(IZosManagerSpi zosManager) {
        ZosBatchManagerImpl.zosManager = zosManager;
    }
    
    protected static IZosmfManagerSpi zosmfManager;
    public static void setZosmfManager(IZosmfManagerSpi zosmfManager) {
        ZosBatchManagerImpl.zosmfManager = zosmfManager;
    }

    private final HashMap<String, ZosBatchImpl> taggedZosBatches = new HashMap<>();
    private final HashMap<String, ZosBatchImpl> zosBatches = new HashMap<>();

    private Path artifactsRoot;
    
    protected static Path archivePath;
    public static void setArchivePath(Path archivePath) {
        ZosBatchManagerImpl.archivePath = archivePath;
    }
    
    protected static String currentTestMethodArchiveFolderName;
    public static void setCurrentTestMethodArchiveFolderName(String folderName) {
        ZosBatchManagerImpl.currentTestMethodArchiveFolderName = folderName;
    }
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosBatchZosmfPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosBatchField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers);
            }
        }
        
        artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi ||
               otherManager instanceof IZosmfManagerSpi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#provisionStart()
     */
    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
        setArchivePath(artifactsRoot.resolve(PROVISIONING).resolve(ZOSBATCH_JOBS));
        setCurrentTestMethodArchiveFolderName("preTest");
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
        cleanup();
        setArchivePath(artifactsRoot.resolve(ZOSBATCH_JOBS));
        if (galasaMethod.getJavaTestMethod() != null) {
            setCurrentTestMethodArchiveFolderName(galasaMethod.getJavaTestMethod().getName() + "." + galasaMethod.getJavaExecutionMethod().getName());
        } else {
            setCurrentTestMethodArchiveFolderName(galasaMethod.getJavaExecutionMethod().getName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestMethod(java.lang.String,java.lang.Throwable)
     */
    @Override
    public String endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull String currentResult, Throwable currentException) throws ManagerException {
        cleanup();
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestClass(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        cleanup();
        setArchivePath(artifactsRoot.resolve(PROVISIONING).resolve(ZOSBATCH_JOBS));
        setCurrentTestMethodArchiveFolderName("postTest");
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestRun()
     */
    @Override
    public void endOfTestRun() {
        try {
            cleanup();
        } catch (ZosBatchException e) {
            logger.error("Problem in endOfTestRun()", e);
        }
    }
    
    protected void cleanup() throws ZosBatchException {
        for (Entry<String, ZosBatchImpl> entry : this.taggedZosBatches.entrySet()) {
            entry.getValue().cleanup();
        }
        for (Entry<String, ZosBatchImpl> entry : this.zosBatches.entrySet()) {
            entry.getValue().cleanup();
        }
    }
    
    @GenerateAnnotatedField(annotation=ZosBatch.class)
    public IZosBatch generateZosBatch(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosBatch annotationZosBatch = field.getAnnotation(ZosBatch.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosBatch.imageTag(), "primary");

        //*** Have we already generated this tag
        if (this.taggedZosBatches.containsKey(tag)) {
            return this.taggedZosBatches.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosBatch zosBatch = new ZosBatchImpl(image);
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
        return newZosBatchJobnameImpl(imageid);
    }

    protected IZosBatchJobname newZosBatchJobnameImpl(String imageid) throws ZosBatchException {
        return new ZosBatchJobnameImpl(imageid);
    }

    @Override
    public @NotNull IZosBatch getZosBatch(IZosImage image) {
        if (zosBatches.containsKey(image.getImageID())) {
            return zosBatches.get(image.getImageID());
        } else {
            ZosBatchImpl zosBatch = new ZosBatchImpl(image);
            zosBatches.put(image.getImageID(), zosBatch);
            return zosBatch;
        }
    }
}
