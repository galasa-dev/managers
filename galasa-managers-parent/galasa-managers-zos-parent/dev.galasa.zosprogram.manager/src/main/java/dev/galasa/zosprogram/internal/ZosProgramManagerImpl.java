/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;
import dev.galasa.zosprogram.internal.properties.ZosProgramPropertiesSingleton;
import dev.galasa.zosprogram.spi.IZosProgramManagerSpi;

@Component(service = { IManager.class })
public class ZosProgramManagerImpl extends AbstractManager implements IZosProgramManagerSpi {
    
    private static final Log logger = LogFactory.getLog(ZosProgramManagerImpl.class);
    
    protected static final String NAMESPACE = "zosprogram";
    
    protected static IZosManagerSpi zosManager;
    protected static void setZosManager(IZosManagerSpi zosManager) {
        ZosProgramManagerImpl.zosManager = zosManager;
    }

    protected static IZosBatchSpi zosBatch;
    protected static void setZosBatch(IZosBatchSpi zosBatchManager) {
        ZosProgramManagerImpl.zosBatch = zosBatchManager;
    }

    protected static IZosFileSpi zosFile;
    protected static void setZosFile(IZosFileSpi zosFileManager) {
        ZosProgramManagerImpl.zosFile = zosFileManager;
    }

    protected static IArtifactManager artifactManager;
    protected static void setArtifactManager(IArtifactManager artifactManager) {
        ZosProgramManagerImpl.artifactManager = artifactManager;
    }

    protected static IBundleResources testBundleResources;
    protected static void setTestBundleResources(IBundleResources testBundleResources) {
        ZosProgramManagerImpl.testBundleResources = testBundleResources;
    }
    protected static IBundleResources getTestBundleResources() {
        return ZosProgramManagerImpl.testBundleResources;
    }

    protected static IBundleResources managerBundleResources;
    protected static void setManagerBundleResources(IBundleResources managerBundleResources) {
        ZosProgramManagerImpl.managerBundleResources = managerBundleResources;
    }
    protected static IBundleResources getManagerBundleResources() {
        return ZosProgramManagerImpl.managerBundleResources;
    }

    private static String runId;    
    public static void setRunId(String runId) {
        ZosProgramManagerImpl.runId = runId;
    }

    protected static IZosDataset runLoadlib;

    private final LinkedHashMap<String, ZosProgramImpl> zosPrograms = new LinkedHashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosProgramPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Unable to request framework services", e);
        }

        if(Boolean.TRUE.equals(galasaTest.isJava())) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosProgramManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers);
            }
        }
        setRunId(getFramework().getTestRunName());
    }
    

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        setZosManager(addDependentManager(allManagers, activeManagers, IZosManagerSpi.class));
        if (zosManager == null) {
            throw new ZosProgramManagerException("The zOS Manager is not available");
        }
        setZosBatch(addDependentManager(allManagers, activeManagers, IZosBatchSpi.class));
        if (zosBatch == null) {
            throw new ZosProgramManagerException("The zOS Batch Manager is not available");
        }
        setZosFile(addDependentManager(allManagers, activeManagers, IZosFileSpi.class));
        if (zosFile == null) {
            throw new ZosProgramManagerException("The zOS File Manager is not available");
        }
        setArtifactManager(addDependentManager(allManagers, activeManagers, IArtifactManager.class));
        if (artifactManager == null) {
            throw new ZosProgramManagerException("The Artifact Manager is not available");
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
    
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosProgramManagerField.class);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestClass()
     */
    @Override
    public void startOfTestClass() throws ManagerException {
        setManagerBundleResources(artifactManager.getBundleResources(this.getClass()));
        setTestBundleResources(artifactManager.getBundleResources(getTestClass()));
        for (Entry<String, ZosProgramImpl> entry : zosPrograms.entrySet()) {
            compile(entry.getValue());
        }
    }
    
    @GenerateAnnotatedField(annotation=ZosProgram.class)
    public IZosProgram generateZosProgram(Field field, List<Annotation> annotations) throws ZosProgramManagerException {
        ZosProgram annotationZosProgram = field.getAnnotation(ZosProgram.class);

        String name = nulled(annotationZosProgram.name());
        String location = nulled(annotationZosProgram.location());
        String tag = defaultString(annotationZosProgram.imageTag(), "primary");
        Language language = annotationZosProgram.language();
        boolean cics = annotationZosProgram.cics();
        String loadlib = nulled(annotationZosProgram.loadlib());
        
        ZosProgramImpl zosProgram = new ZosProgramImpl(field, tag, name, location, language, cics, loadlib);
        zosPrograms.put(field.getName(), zosProgram);
        
        return zosProgram;
    }


    @Override
    public IZosProgram newZosProgram(IZosImage image, String name, String programSource, Language language, boolean cics, String loadlib) throws ZosProgramManagerException {
        return new ZosProgramImpl(image, name, programSource, language, cics, loadlib);
    }
    
    @Override
    public IZosProgram compile(IZosProgram zosProgram) throws ZosProgramManagerException {
        logger.info("Compile " + zosProgram.getLanguage() + " program \"" + zosProgram.getName() + "\"" + ((ZosProgramImpl) zosProgram).logForField());
        switch (zosProgram.getLanguage()) {
        case COBOL:
            ZosCobolProgramCompiler cobolProgram = new ZosCobolProgramCompiler((ZosProgramImpl) zosProgram);
            cobolProgram.compile();
            break;
        default:
            throw new ZosProgramManagerException("Invalid program language: " + zosProgram.getLanguage());
        }
        return zosProgram;
    }


    public static IZosBatch getZosBatch(IZosImage image) {
        return zosBatch.getZosBatch(image);
    }
    
    public static IZosDataset getRunLoadlib(IZosImage image) throws ZosProgramManagerException {
        if (runLoadlib == null) {
            try {
                runLoadlib = zosFile.getZosFileHandler().newDataset(zosManager.getRunDatasetHLQ(image) + "." + runId + ".LOAD", image);
                if (!runLoadlib.exists()) {
                    runLoadlib.setSpace(SpaceUnit.CYLINDERS, 1, 5);
                    runLoadlib.setRecordFormat(RecordFormat.UNDEFINED);
                    runLoadlib.setRecordlength(0);
                    runLoadlib.setBlockSize(32720);
                    runLoadlib.setDatasetOrganization(DatasetOrganization.PARTITIONED);
                    runLoadlib.setDatasetType(DSType.LIBRARY);
                    runLoadlib.createRetainTemporary();
                }
            } catch (ZosManagerException e) {
                throw new ZosProgramManagerException(e);
            }
        }
        return runLoadlib;
    }
}
