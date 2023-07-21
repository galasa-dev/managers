/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;

import com.ibm.hursley.cicsts.test.sem.complex.Complex;
import com.ibm.hursley.cicsts.test.sem.complex.RunOptions;
import com.ibm.hursley.cicsts.test.sem.complex.jcl.JCLException;
import com.ibm.hursley.cicsts.test.sem.complex.jcl.Job;

import conrep.CICS;
import conrep.ConRep;
import conrep.MVS;
import conrep.impl.ConrepPackageImpl;
import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.MasType;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicsRegionProvisioner;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.sem.DoNotBuild;
import dev.galasa.sem.DoNotStartCICS;
import dev.galasa.sem.SemManagerException;
import dev.galasa.sem.SemTopology;
import dev.galasa.sem.internal.properties.BaseModel;
import dev.galasa.sem.internal.properties.CicsBuild;
import dev.galasa.sem.internal.properties.InteralVersion;
import dev.galasa.sem.internal.properties.ModelUrl;
import dev.galasa.sem.internal.properties.SemPropertiesSingleton;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosconsole.spi.IZosConsoleSpi;
import sem.DEFCICS;
import sem.Environment;
import sem.SemFactory;
import sem.SymGroup;
import sem.Symbolic;
import sem.impl.SemPackageImpl;

@Component(service = { IManager.class })
public class SemManagerImpl extends AbstractManager implements ICicsRegionProvisioner {
    protected static final String NAMESPACE = "sem";

    private static final Log logger = LogFactory.getLog(SemManagerImpl.class);
    private boolean required;

    private IDynamicStatusStoreService dss;

    private IZosManagerSpi zosManager;
    private IZosBatchSpi zosBatch;
    private IZosConsoleSpi zosConsole;
    private ICicstsManagerSpi cicsManager;
    private IArtifactManager artifactManager;
    private IHttpManagerSpi  httpManager;

    private SemTopology semTopology;

    private ArrayList<Environment> environments = new ArrayList<>();

    private Complex     complex      = new Complex();
    private SemPoolResolver poolResovler;
    private final CsdInputGenerator csdGenerator = new CsdInputGenerator(this);
    private final SitGenerator      sitGenerator = new SitGenerator(this);

    private ConRep conrep;

    private HashMap<String, SemCicsImpl> taggedRegions = new HashMap<>();
    private HashMap<String, SemCicsImpl> applidRegions = new HashMap<>();

    private IBundleResources semBundleResources;
    private IHttpClient      httpClient;

    private GalasaTest galasaTest;

    private HashMap<CICS, List<Job>> runtimeJobs;
    private List<Job> discardJobs;
    private List<Job> buildJobs;
    private SemZosHandler semZosHandler;

    private IZosImage primaryZosImage;
    private IZosImage secondaryZosImage;

    private final HashMap<String, IZosBatchJob> runningJobs = new HashMap<>();

    private String consoleName; // TODO the zos console manager should be generating random names

    private boolean doNotBuild = false;
    private boolean doNotStart = false;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if (!required) {
            if(galasaTest.isJava()) {
                Class<?> testClass = galasaTest.getJavaTestClass();

                this.semTopology = testClass.getAnnotation(SemTopology.class);
                if (this.semTopology == null) {
                    return; // Not required
                }
            } else {
                return; // Dont support anything other than Java at the moment
            }
        }

        this.galasaTest = galasaTest;

        youAreRequired(allManagers, activeManagers, galasaTest);

        try {
            SemPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
            this.dss = getFramework().getDynamicStatusStoreService(NAMESPACE);
        } catch (ConfigurationPropertyStoreException | DynamicStatusStoreException e) {
            throw new CicstsManagerException("Unable to request framework services", e);
        }

        semBundleResources = this.artifactManager.getBundleResources(getClass());
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, GalasaTest galasaTest)
            throws ManagerException {
        super.youAreRequired(allManagers, activeManagers, galasaTest);

        if (activeManagers.contains(this)) {
            return;
        }

        this.required = true;
        activeManagers.add(this);

        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (this.zosManager == null) {
            throw new SemManagerException("Unable to locate the zOS Manager, required for the SEM Manager");
        }

        this.zosBatch = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (this.zosBatch == null) {
            throw new SemManagerException("Unable to locate the zOS Batch Manager, required for the SEM Manager");
        }

        this.zosConsole = addDependentManager(allManagers, activeManagers, galasaTest, IZosConsoleSpi.class);
        if (this.zosConsole == null) {
            throw new SemManagerException("Unable to locate the zOS Console Manager, required for the SEM Manager");
        }

        this.cicsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
        if (this.cicsManager == null) {
            throw new SemManagerException("Unable to locate the CICS TS Manager, required for the SEM Manager");
        }

        this.artifactManager = addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
        if (this.artifactManager == null) {
            throw new SemManagerException("Unable to locate the Artifact Manager, required for the SEM Manager");
        }

        this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new SemManagerException("Unable to locate the Http Manager, required for the SEM Manager");
        }

        // Register this as a provisioner
        this.cicsManager.registerProvisioner(this);

        this.consoleName = "GAL" + getFramework().getTestRunName();
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {

        if (otherManager == this.zosManager) {
            return true;
        }

        if (otherManager == this.zosBatch) {
            return true;
        }

        if (otherManager == this.cicsManager) {
            return true;
        }

        return false;
    }

    @Override
    public void cicsProvisionGenerate() throws ManagerException, ResourceUnavailableException {
        Class<?> testClass = this.galasaTest.getJavaTestClass();
        if (testClass.isAnnotationPresent(DoNotBuild.class)) {
            this.doNotBuild = true;
            this.doNotStart = true;
        }
        if (testClass.isAnnotationPresent(DoNotStartCICS.class)) {
            this.doNotStart = true;
        }


        String provisionType = this.cicsManager.getProvisionType().toLowerCase();
        switch(provisionType) {
            case "sem":
            case "provisioned":
            case "mixed":
                break;
            default:
                return;
        }

        if (this.semTopology == null) {
            return;
        }


        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
                new XMIResourceFactoryImpl());
        SemPackageImpl.init();
        ConrepPackageImpl.init();

        logger.info("Starting CICS TS SEM provisioning");

        this.poolResovler = new SemPoolResolver(this);
        this.complex.setPoolResolver(this.poolResovler);


        String primaryImageTag = this.semTopology.imageTag();
        String secondaryImageTag = this.semTopology.secondaryImageTag();

        primaryZosImage = this.zosManager.provisionImageForTag(primaryImageTag);
        logger.info("Using " + primaryZosImage + " as the default system for the SEM complex");
        secondaryZosImage = null;
        if (!secondaryImageTag.isEmpty()) {
            secondaryZosImage = this.zosManager.getImageForTag(secondaryImageTag);
            logger.info("Using " + secondaryZosImage + " as the secondary zOS Image, available via the symbolic variable &SECONDARY_SYSTEM");
        }

        ProductVersion primaryVersion = this.cicsManager.getDefaultVersion();
        String actualVersion = InteralVersion.get(primaryVersion);
        logger.info("Using " + actualVersion + " as primary version for the SEM complex");

        logger.trace("Loading SEM models"); 

        logger.trace("Loading topology model");

        fetchSemModel(semTopology.model());

        // Add the default settings
        Environment defaultEnvironment = SemFactory.eINSTANCE.createEnvironment();
        this.environments.add(defaultEnvironment);
        defaultEnvironment.setDefaulttab(SemFactory.eINSTANCE.createDefaultTab());
        defaultEnvironment.setParamtab(SemFactory.eINSTANCE.createParamTab());

        SymGroup defaultSymbolics = SemFactory.eINSTANCE.createSymGroup();
        defaultEnvironment.getParamtab().getSYMGROUPs().add(defaultSymbolics);
        defaultSymbolics.setName("Galasa Symbolics");

        DEFCICS defaultCics = SemFactory.eINSTANCE.createDEFCICS();
        defaultEnvironment.getDefaulttab().getCICSs().add(defaultCics);
        defaultCics.setName("Galasa CICS Settings");
        defaultCics.setCicsversion(actualVersion);
        defaultCics.setSystem(primaryZosImage.getImageID());

        if (secondaryZosImage != null) {
            Symbolic secondarySymbolic = SemFactory.eINSTANCE.createSymbolic();
            secondarySymbolic.setGROUP(defaultSymbolics);
            secondarySymbolic.setName("SECONDARY_SYSTEM");
            secondarySymbolic.setValue(secondaryZosImage.getImageID());
        }

        // Set the Base versions
        Symbolic baseCicsVersionSymbolic = SemFactory.eINSTANCE.createSymbolic();
        baseCicsVersionSymbolic.setGROUP(defaultSymbolics);
        baseCicsVersionSymbolic.setName("BASE_CICSVERSION");
        baseCicsVersionSymbolic.setValue(actualVersion);

        Symbolic baseCpsmVersionSymbolic = SemFactory.eINSTANCE.createSymbolic();
        baseCpsmVersionSymbolic.setGROUP(defaultSymbolics);
        baseCpsmVersionSymbolic.setName("BASE_CPSMVERSION");
        baseCpsmVersionSymbolic.setValue(actualVersion);

        // Set the TESTNAME, used for the datasets
        Symbolic testNameSymbolic = SemFactory.eINSTANCE.createSymbolic();
        testNameSymbolic.setGROUP(defaultSymbolics);
        testNameSymbolic.setName("TESTNAME");
        testNameSymbolic.setValue(getFramework().getTestRunName());

        // Set the userid 
        Symbolic useridSymbolic = SemFactory.eINSTANCE.createSymbolic();
        useridSymbolic.setGROUP(defaultSymbolics);
        useridSymbolic.setName("USERID");
        useridSymbolic.setValue(((CredentialsUsernamePassword)primaryZosImage.getDefaultCredentials()).getUsername());

        // Set the HLQ
        String hlq = this.zosManager.getRunDatasetHLQ(primaryZosImage);

        Symbolic hlqSymbolic = SemFactory.eINSTANCE.createSymbolic();
        hlqSymbolic.setGROUP(defaultSymbolics);
        hlqSymbolic.setName("HLQ");
        hlqSymbolic.setValue(hlq);

        // Set the uss directory
        String ussDirectory = this.zosManager.getRunUNIXPathPrefix(primaryZosImage);

        Symbolic ussSymbolic = SemFactory.eINSTANCE.createSymbolic();
        ussSymbolic.setGROUP(defaultSymbolics);
        ussSymbolic.setName("TEMPORARY_DIRECTORY");
        ussSymbolic.setValue(ussDirectory);

        // Setup any z/OS provisioned port symbolics
        for (String portTag : this.zosManager.getTaggedPorts().keySet()) {
        	Symbolic portSymbolic = SemFactory.eINSTANCE.createSymbolic();
        	portSymbolic.setGROUP(defaultSymbolics);
        	portSymbolic.setName(portTag);
        	portSymbolic.setValue("" + this.zosManager.getTaggedPorts().get(portTag));
        }

        // Add the BUILD sem model
        String cicsBuild = CicsBuild.get();
        if (cicsBuild != null) {
            fetchSemModel("Version_" + actualVersion + "_Build_" + cicsBuild);
        }

        // Add the Version sem model
        fetchSemModel("Version_" + actualVersion);

        // Add the primary Image sem model
        fetchSemModel("Plex_" + primaryZosImage.getSysplexID() + "_Image_" + primaryZosImage.getImageID());

        // Add the primary Sysplex sem model
        fetchSemModel("Plex_" + primaryZosImage.getSysplexID());

        if (secondaryZosImage != null) {
            // Add the secondary Image sem model
            fetchSemModel("Plex_" + secondaryZosImage.getSysplexID() + "_Image_" + secondaryZosImage.getImageID());

            if (!primaryZosImage.getSysplexID().equals(secondaryZosImage.getSysplexID())) {
                // Add the primary Sysplex sem model if different from primary
                fetchSemModel("Plex_" + secondaryZosImage.getSysplexID());
            }
        }

        // Add the base model
        fetchSemModel(BaseModel.get());

        // Add the CSD Inputs to the Model

        this.csdGenerator.generate(defaultEnvironment, this.galasaTest.getJavaTestClass());

        // Add the SITs to the Model

        this.sitGenerator.generate(defaultEnvironment, this.galasaTest.getJavaTestClass());

        // Add the zos interfaces

        this.semZosHandler = new SemZosHandler(this.zosManager, this.zosBatch, this.primaryZosImage, this.secondaryZosImage);

        // Generate the model
        generateComplex();

        this.poolResovler.generateComplete();

    }


    private void generateComplex() throws SemManagerException {
        try {
            RunOptions options = new RunOptions(LogManager.getLoggerRepository());
            options.setBuildComplex(true);
            options.setJobPurge(false);
            options.setConrepRequired(false);
            options.setBuildCICSplex(true);
            options.setJobPrefix("GAL");

            int rc = this.complex.buildComplex(environments, options, LogManager.getLoggerRepository());

            if (rc > 4) {
                throw new SemManagerException("SEM complex generation failed, rc=" +rc);
            }

            this.conrep = this.complex.generateConRepModel();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            this.complex.reportConfig(ps);
            ps.close();
            baos.close();

            String configReport = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            logger.info("SEM configuration report:-\n" + configReport);     

            for(CICS cics : this.conrep.getCICSs().getCICSs()) {

                // Get the TAG if there is one
                String cicsTag = ""; // default just in case
                if (!cics.getTag().isEmpty()) {
                    cicsTag = cics.getTag().get(0).getTag();
                }

                cicsTag = cicsTag.toUpperCase();

                MVS mvs = cics.getMVS();
                if (mvs == null) {
                    throw new SemManagerException("SEM provisioned CICS region is missing system - '" + cics.getApplid() + "'");
                } 
                String jesid = mvs.getJesid();
                if (jesid == null) {
                    throw new SemManagerException("SEM provisioned CICS region is missing jesid - '" + cics.getApplid() + "'");
                } 

                IZosImage zosImage = null;
                if (this.primaryZosImage.getSysname().equals(jesid)) {
                    zosImage = this.primaryZosImage;
                } else if (this.secondaryZosImage.getSysname().equals(jesid)) {
                    zosImage = this.secondaryZosImage;
                } else {
                    throw new SemManagerException("SEM provisioned CICS region is has a jesid different to primary or secondary z/OS images - '" + cics.getApplid() + "'");
                }

                MasType masType = null;
                switch(cics.getType().getType()) {
                    case CICS:
                        masType = MasType.CICS;
                        break;
                    case CMAS:
                        masType = MasType.CMAS;
                        break;
                    case LMAS:
                        masType = MasType.LMAS;
                        break;
                    case WUI:
                        masType = MasType.WUI;
                        break;
                    default:
                        throw new SemManagerException("Unrecognised CICS region type '" + cics.getType().getType() + "'");
                }

                SemCicsImpl region = new SemCicsImpl(this, this.cicsManager, this.semZosHandler, this.complex, cics, zosImage, cicsTag, masType, !this.doNotStart);
                String applid = cics.getApplid().getApplid();
                this.applidRegions.put(applid, region);

                if (!cicsTag.isEmpty()) {               
                    logger.info("Provisioned " + region + " for tag " + cicsTag);

                    this.taggedRegions.put(cicsTag, region);

                    if (!cics.getTag().isEmpty()) {
                        for(int i = 1; i < cics.getTag().size(); i++) {
                            String tag = cics.getTag().get(i).getTag().toUpperCase();
                            this.taggedRegions.put(tag, region);
                            logger.info("Provisioned " + region + " for tag " + tag);
                        }
                    }
                }

                this.poolResovler.getApplidPool().setSystem(applid, zosImage.getSysname());
            }

        } catch(SemManagerException e) {
            throw e;
        } catch(Exception e) {
            throw new SemManagerException("Problem generating SEM complex", e);
        }

    }

    private void fetchSemModel(@NotNull String model) throws SemManagerException {
        if (!model.endsWith(".sem")) {
            model += ".sem";
        }
        // First, check to see if it is in the test bundle
        IBundleResources testBundleResources = this.artifactManager.getBundleResources(getTestClass());
        try {
            String modelString = testBundleResources.retrieveFileAsString(model);
            this.environments.add(convertModel(modelString));
        	logger.trace("Located SEM model '" + model + "' in test bundle");
            return;
        } catch (TestBundleResourceException e) {
        	logger.trace("Did not find SEM model '" + model + "' in test bundle");
            // Ignore because it may not exist in the bundle
        } catch (IOException e) {
            throw new SemManagerException("Unable to read SEM model '" + model + "' from test bundle", e);
        }

        // Now, check to see if it is in the manager bundle
        try {
            String modelString = semBundleResources.retrieveFileAsString(model);
            this.environments.add(convertModel(modelString));
        	logger.trace("Located SEM model '" + model + "' in manager bundle");
            return;
        } catch (TestBundleResourceException e) {
            // Ignore because it may not exist in the bundle
        	logger.trace("Did not find SEM model '" + model + "' in manager bundle");
        } catch (IOException e) {
            throw new SemManagerException("Unable to read SEM model '" + model + "' from manager bundle", e);
        }
        
        // Find the model from an online server
        if (this.httpClient == null) {
        	this.httpClient = this.httpManager.newHttpClient();
        	try {
				this.httpClient.setURI(ModelUrl.get().toURI());
			} catch (URISyntaxException e) {
				throw new SemManagerException("Badly formed URI for the sem.model.url", e);
			}
        }
        
        String modelUrl = ModelUrl.get() + "/" + model;
        
        try {
        	HttpClientResponse<String> response = this.httpClient.getText(model);
        	
        	if (response.getStatusCode() == 200) {
                this.environments.add(convertModel(response.getContent()));
            	logger.trace("Located SEM model '" + model + "' on website");
                return;
        	} else if (response.getStatusCode() == 404) {
                // Ignore because it may not exist in the website
            	logger.trace("Did not find SEM model '" + model + "' on website");
        	} else {
        		throw new SemManagerException("Unable to read SEM model '" + model + "' from url " + modelUrl + " - " + response.getStatusLine());
        	}
        } catch(HttpClientException e) {
        	throw new SemManagerException("Unable to read SEM model '" + model + "' from url " + modelUrl,e);
        }

        throw new SemManagerException("Unable to locate the SEM model '" + model + "'");
    }

    @Override
    public ICicsRegionProvisioned provision(@NotNull String cicsTag, @NotNull String imageTag,
            @NotNull List<Annotation> annotations) throws ManagerException {

        return this.taggedRegions.get(cicsTag.toUpperCase());
    }

    @Override
    public void cicsProvisionBuild() throws ManagerException, ResourceUnavailableException {
        if (this.doNotBuild) {
            logger.info("Ignoring build of complex as @SemDoNotBuild is present");
            return;
        }

        String provisionType = this.cicsManager.getProvisionType().toLowerCase();
        switch(provisionType) {
            case "sem":
            case "provisioned":
            case "mixed":
                break;
            default:
                return;
        }
        
        // TODO do we need to replicate provisioning type of Resolved?

        // TODO feature toggle support?

        try {
            complex.registerFileProvider(this.semZosHandler);
            complex.registerJobProvider(this.semZosHandler);

            runtimeJobs = complex.getRuntimeJobMap(complex, conrep);
            discardJobs = complex.getDiscardJobs(environments);

            logger.info("Checking for running CICS Regions");
            checkForRunningJobs();

            // add all jobs to run dynamics

            buildJobs = complex.getBuildJobs(environments);

            if (!complex.runJobs(buildJobs)) {
                logger.fatal("Some build jobs failed");
                retrieveOutput("sem/buildjob/", this.semZosHandler.getJobs());
                throw new SemManagerException("Some build jobs have failed");
            }

            retrieveOutput("sem/buildjob/", this.semZosHandler.getJobs());

        } catch (Exception e) {
            if (e.getMessage().equals("got into a no running jobs state!!!")) {
                retrieveOutput("sem/buildjob/", this.semZosHandler.getJobs());
            }

            throw new SemManagerException("Build of CICS complex failed", e);
        }


    }



    private void retrieveOutput(String path, List<IZosBatchJob> jobs) {
        for(IZosBatchJob job : jobs) {
            try {
                job.saveOutputToResultsArchive(path);
            } catch (ZosBatchException e) {
                logger.error("Failed to archive output from " + job);
            }

            try {
                job.purge();
            } catch (ZosBatchException e) {
                logger.warn("Failed to purge " + job);
            }
        }

    }

    private void checkForRunningJobs() throws SemManagerException {
        boolean failedCancel = false;

        IZosBatch batch = this.zosBatch.getZosBatch(this.primaryZosImage);

        for (Entry<CICS, List<Job>> cicsRuntimeJobs : runtimeJobs.entrySet()) {
            for (Job job : cicsRuntimeJobs.getValue()) {
                try {
                    List<IZosBatchJob> possibleJobs = batch.getJobs(job.getJobname(), "*");
                    for(IZosBatchJob possibleJob : possibleJobs) {
                        if (possibleJob.getStatus() == JobStatus.ACTIVE) {
                            logger.info("Cancelling pre running CICS Region '"
                                    + possibleJob.getJobname() + "(" + possibleJob.getJobId() + ")'");
                            possibleJob.cancel();
                        }
                    }
                } catch (Exception e) {
                    logger.error(
                            "Failed to cancel existing job " + job.getJobname(),
                            e);
                    failedCancel = true;
                }
            }
        }
        if (failedCancel) {
            throw new SemManagerException("Failed to cancel existing jobs");
        }

        return;
    }


    @Override
    public void cicsProvisionStart() throws ManagerException, ResourceUnavailableException {
        if (this.doNotBuild) {
            logger.info("Ignoring start of complex as @SemDoNotBuild is present");
            return;
        }
        if (this.doNotStart) {
            logger.info("Ignoring start of complex as @DoNotStartCICS is present");
            return;
        }

        ArrayList<ICicsRegion> cicss = new ArrayList<>(this.applidRegions.values());
        startupCics(cicss);
    }

    private void startupCics(List<ICicsRegion> regions) throws SemManagerException {
        ArrayList<SemCicsImpl> submittedRegions = new ArrayList<>(regions.size());        
        for(ICicsRegion oRegion : regions) {
            if (!(oRegion instanceof SemCicsImpl)) {
                continue; // Ignore regions from other provisioners
            }

            SemCicsImpl region = (SemCicsImpl) oRegion;
            region.startup();  // TODO multithread startup and wait
            submittedRegions.add(region);
        }
    }

    @Override
    public void cicsProvisionStop() {
        if (this.doNotBuild) {
            logger.info("Ignoring stop of complex as @SemDoNotBuild is present");
            return;
        }

        logger.info("Stopping all SEM provisioned CICS regions");
        ArrayList<SemCicsImpl> regions = new ArrayList<>(this.applidRegions.values());
        for(SemCicsImpl region : regions) {
            try { 
                region.shutdown(); // TODO multithread shutdown and wait
            } catch(SemManagerException e) {
                logger.error("Shutdown of CICS TS region " + region.getApplid() + " failed");
            }
        }
        logger.info("All SEM provisioned CICS regions have stopped");
    }

    @Override
    public void cicsProvisionDiscard() {
        if (this.doNotBuild) {
            logger.info("Ignoring discard of complex as @SemDoNotBuild is present");
            return;
        }

        String provisionType = this.cicsManager.getProvisionType().toLowerCase();
        switch(provisionType) {
            case "sem":
            case "provisioned":
            case "mixed":
                break;
            default:
                return;
        }
        
        logger.info("Discarding SEM complex");
        this.semZosHandler.clearJobs();

        // Run the discard jobs
        for(Job job : this.discardJobs) {
            try {
                this.semZosHandler.submitJob(job);
            } catch (JCLException e) {
                logger.error("Unable to submit discard job");
            }
        }

        Instant expire = Instant.now().plus(20, ChronoUnit.MINUTES); // time out after 10 minutes
        // Wait for them to complete
        List<IZosBatchJob> waiting = this.semZosHandler.getJobs();
        ArrayList<IZosBatchJob> completed = new ArrayList<>(waiting.size());
        while(Instant.now().isBefore(expire)) {
            Iterator<IZosBatchJob> jobi = waiting.iterator();
            while(jobi.hasNext()) {
                IZosBatchJob job = jobi.next();

                if (job.getStatus() == JobStatus.OUTPUT) {
                    completed.add(job);
                    jobi.remove();
                }
            }

            if (waiting.isEmpty()) {
                break;
            }
        }

        // record the output
        retrieveOutput("sem/discard/", completed);
        retrieveOutput("sem/discard_not_finished/", waiting);

        this.poolResovler.discard();

        super.provisionDiscard();

        logger.info("SEM discard is complete");
    }








    public Environment convertModel(@NotNull String modelString) throws SemManagerException {
        ByteArrayInputStream bais = new ByteArrayInputStream(modelString.getBytes());

        ResourceSet resSet = new ResourceSetImpl();
        HashMap<String, String> dummyOptions = new HashMap<String, String>();

        Resource resource = resSet.createResource(org.eclipse.emf.common.util.URI.createURI("sem://" + UUID.randomUUID().toString()));
        try {
            resource.load(bais, dummyOptions);
        } catch (IOException e) {
            throw new SemManagerException("Unable to load the SEM model ", e);
        }

        return (Environment)resource.getContents().get(0);
    }

    public IDynamicStatusStoreService getDss() {
        return this.dss;
    }

    protected IArtifactManager getArtifactManager() {
        return this.artifactManager;
    }

    protected ICicstsManagerSpi getCicsManager() {
        return this.cicsManager;
    }

    protected IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    protected IZosBatchSpi getZosBatch() {
        return this.zosBatch;
    }

    public IZosConsoleSpi getZosConsoleManager() {
        return this.zosConsole;
    }

}
