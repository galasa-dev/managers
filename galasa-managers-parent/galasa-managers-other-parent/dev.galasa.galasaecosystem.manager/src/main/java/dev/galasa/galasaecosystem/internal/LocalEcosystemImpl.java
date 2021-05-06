package dev.galasa.galasaecosystem.internal;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.ILocalEcosystem;
import dev.galasa.galasaecosystem.internal.properties.MavenRepo;
import dev.galasa.galasaecosystem.internal.properties.MavenUseDefaultLocalRepository;
import dev.galasa.galasaecosystem.internal.properties.MavenVersion;
import dev.galasa.java.IJavaInstallation;

public abstract class LocalEcosystemImpl extends AbstractEcosystemImpl implements ILocalEcosystem {

    private final Log                           logger = LogFactory.getLog(getClass());

    private Path runHome;
    private Path galasaDirectory;
    private Path bootstrapFile;
    private Path cpsFile;
    private Path dssFile;
    private Path rasDirectory;
    private Path credentialsFile;
    private Path overridesFile;

    private Path bootJar;
    private Path simplatformJar;

    private URL mavenRepo;
    private String mavenVersion;
    private Path mavenLocal;

    private final Gson gson = GalasaGsonBuilder.build();

    private final ArrayList<LocalRun> localRuns = new ArrayList<>();

    public LocalEcosystemImpl(GalasaEcosystemManagerImpl manager, 
            String tag,
            IJavaInstallation javaInstallation) {
        super(manager, tag, javaInstallation);
    }


    protected void build(Path runHomeDirectory, Path homeDirectory) throws GalasaEcosystemManagerException {
        try {
            this.runHome = runHomeDirectory;
            this.galasaDirectory = this.runHome.resolve("galasaconfig");
            this.bootstrapFile = this.galasaDirectory.resolve("bootstrap.properties");
            this.cpsFile = this.galasaDirectory.resolve("cps.properties");
            this.dssFile = this.galasaDirectory.resolve("dss.properties");
            this.rasDirectory = this.galasaDirectory.resolve("ras");
            this.credentialsFile = this.galasaDirectory.resolve("credentials.properties");
            this.overridesFile = this.galasaDirectory.resolve("overrides.properties");

            Files.createDirectory(galasaDirectory);

            // Create the bootstrap file

            Properties boostrapProperties = new Properties();
            boostrapProperties.setProperty("framework.config.store", "file:" + this.cpsFile.toString());            
            boostrapProperties.store(Files.newOutputStream(this.bootstrapFile, StandardOpenOption.CREATE_NEW), "Galasa Ecosystem Manager");

            // Create the cps file

            Properties cpsProperties = new Properties();
            cpsProperties.setProperty("framework.dynamicstatus.store", "file:" + this.dssFile.toString());            
            cpsProperties.setProperty("framework.resultarchive.store", "file:" + this.rasDirectory.toString());            
            cpsProperties.setProperty("framework.credentials.store", "file:" + this.credentialsFile.toString());            
            cpsProperties.store(Files.newOutputStream(this.cpsFile, StandardOpenOption.CREATE_NEW), "Galasa Ecosystem Manager");

            // Create the dss file
            Properties dssProperties = new Properties();
            dssProperties.store(Files.newOutputStream(this.dssFile, StandardOpenOption.CREATE_NEW), "Galasa Ecosystem Manager");

            // create the ras directory
            Files.createDirectory(this.rasDirectory);

            // Create the creds file
            Properties credsProperties = new Properties();
            credsProperties.store(Files.newOutputStream(this.credentialsFile, StandardOpenOption.CREATE_NEW), "Galasa Ecosystem Manager");

            // Create the overrides file
            Properties overridesProperties = new Properties();
            overridesProperties.store(Files.newOutputStream(this.overridesFile, StandardOpenOption.CREATE_NEW), "Galasa Ecosystem Manager");

            // Download all the artifacts we need from Maven

            this.mavenRepo = MavenRepo.get();
            this.mavenVersion = MavenVersion.get();

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("MAVEN_REPO", this.mavenRepo.toString());
            parameters.put("MAVEN_VERSION", this.mavenVersion);

            if (!MavenUseDefaultLocalRepository.get()) {
                this.mavenLocal = this.galasaDirectory.resolve("repository");
            } else {
                this.mavenLocal = homeDirectory.resolve(".m2/repository");
            }
            parameters.put("MAVEN_LOCAL", this.mavenLocal.toString());

            IArtifactManager artifactManager = getEcosystemManager().getArtifactManager();
            IBundleResources bundleResources = artifactManager.getBundleResources(this.getClass());

            Path settingsXml = this.galasaDirectory.resolve("settings.xml");
            Path pomXml = this.galasaDirectory.resolve("pom.xml");

            Files.copy(bundleResources.retrieveSkeletonFile("maven/settings.xml", parameters), settingsXml);
            Files.copy(bundleResources.retrieveSkeletonFile("maven/pom.xml", parameters), pomXml);

            StringBuilder fetchCommand = new StringBuilder();
            fetchCommand.append("mvn");
            fetchCommand.append(" -B");
            fetchCommand.append(" -U");
            //            fetchCommand.append(" --ntp");  18.04 of ubuntu doesn't have this,  suspect windows as well
            fetchCommand.append(" --settings ");
            fetchCommand.append(settingsXml.toString());
            fetchCommand.append(" -f ");
            fetchCommand.append(pomXml.toString());
            fetchCommand.append(" process-sources");

            String response = this.getCommandShell().issueCommand(fetchCommand.toString());
            if (!response.contains("BUILD SUCCESS")) {
                throw new GalasaEcosystemManagerException("Problem installing the required artifacts from Maven:-\n" + response);
            }

            this.bootJar = this.galasaDirectory.resolve("boot.jar");
            this.simplatformJar = this.galasaDirectory.resolve("simplatform.jar");

            logger.info("Galasa local ecosystem has been installed");

        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem building the Local Ecosystem",e);
        }
    }

    @Override
    public JsonObject waitForRun(String runName) throws GalasaEcosystemManagerException {
        return waitForRun(runName, 3);
    }

    @Override
    public JsonObject waitForRun(String runName, int minutes) throws GalasaEcosystemManagerException {
        LocalRun localRun = null;
        for(LocalRun run : this.localRuns) {
            if (run.getRunName().equals(runName)) {
                localRun = run;
                break;
            }
        }

        if (localRun == null) {
            throw new GalasaEcosystemManagerException("Unable to locate submitted run " + runName);
        }

        Path rasStructure = this.rasDirectory.resolve(runName).resolve("structure.json");

        try {
            Instant expire = Instant.now().plus(minutes, ChronoUnit.MINUTES);
            while(expire.isAfter(Instant.now())) {
                if (Files.exists(rasStructure)) {
                    try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(rasStructure))) {
                        JsonObject jsonRun = gson.fromJson(reader, JsonObject.class);
                        JsonElement oresult = jsonRun.get("status");
                        if (oresult != null) {
                            if ("finished".equals(oresult.getAsString())) {
                                return jsonRun;
                            }
                        }

                    }
                }

                Thread.sleep(2000);
            }

            throw new GalasaEcosystemManagerException("Run name " + runName + " did not finish in time");
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Unable to determine the status of run name " + runName, e);
        }
    }

    private String convertInstant(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return Instant.parse(value).toString();
    }


    @Override
    public String getCpsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCpsProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDssProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDssProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCredsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCredsProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub

    }

    protected Path getBootJar() {
        return this.bootJar;
    }

    protected Path getSimplatformJar() {
        return this.simplatformJar;
    }

    protected Path getBootstrapFile() {
        return this.bootstrapFile;
    }

    protected Path getGalasaConfigDirectory() {
        return this.galasaDirectory;
    }

    protected URL getMavenRepo() {
        return this.mavenRepo;
    }

    protected Path getMavenLocal() {
        return this.mavenLocal;
    }

    protected String getMavenVersion() {
        return this.mavenVersion;
    }

    protected void addLocalRun(@NotNull LocalRun localRun) {
        this.localRuns.add(localRun);
    }

    public void discard() {

        // save all data in the stored artifacts

        Path saRoot = getEcosystemManager().getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        Path saEcosystem = saRoot.resolve("ecosystem");

        // CPS
        try {
            Files.copy(this.cpsFile, saEcosystem.resolve(cpsFile.getFileName().toString()));
        } catch(Exception e) {
            logger.warn("Failed to save the local ecosystem CPS",e);
        }

        // DSS
        try {
            Files.copy(this.dssFile, saEcosystem.resolve(dssFile.getFileName().toString()));
        } catch(Exception e) {
            logger.warn("Failed to save the local ecosystem DSS",e);
        }

        // overrides
        try {
            Files.copy(this.overridesFile, saEcosystem.resolve(overridesFile.getFileName().toString()));
        } catch(Exception e) {
            logger.warn("Failed to save the local ecosystem overrides",e);
        }

        // bootstrap
        try {
            Files.copy(this.bootstrapFile, saEcosystem.resolve(bootstrapFile.getFileName().toString()));
        } catch(Exception e) {
            logger.warn("Failed to save the local ecosystem bootstrap",e);
        }

        logger.info("Not saving credentials into stored artifacts for security reasons");

        // copy all the run data
        for(LocalRun run : this.localRuns) {
            String runName = run.getRunName();

            Path rasRun = this.rasDirectory.resolve(runName);
            Path saRun = saEcosystem.resolve("runs").resolve(runName);

            try {
                Path runLog = rasRun.resolve("run.log");
                if (Files.exists(runLog)) {
                    Files.copy(runLog, saRun.resolve(runLog.getFileName().toString()));
                }
            } catch(Exception e) {
                logger.warn("Failed to copy run " + runName + " run log",e);
            }

            try {
                Path structure = rasRun.resolve("structure.json");
                if (Files.exists(structure)) {
                    Files.copy(structure, saRun.resolve(structure.getFileName().toString()));
                }
            } catch(Exception e) {
                logger.warn("Failed to copy run " + runName + " structure json",e);
            }

            try {
                Path artifacts = rasRun.resolve("artifacts.properties");
                if (Files.exists(artifacts)) {
                    Files.copy(artifacts, saRun.resolve(artifacts.getFileName().toString()));
                }
            } catch(Exception e) {
                logger.warn("Failed to copy run " + runName + " artifacts properties",e);
            }


            try {
                Properties artifacts = new Properties();
                Path artifactsFile = rasRun.resolve("artifacts.properties");
                Path artifactsDirectory = rasRun.resolve("artifacts");
                artifacts.load(Files.newInputStream(artifactsFile));

                for(Entry<Object, Object> entry : artifacts.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();

                    try {
                        String artifactPath = key.substring(1);

                        Path saArtifact = saRun.resolve(artifactPath);
                        Files.createDirectories(saArtifact.getParent());
                        Path rasArtifact = artifactsDirectory.resolve(artifactPath);
                        ResultArchiveStoreContentType type = new ResultArchiveStoreContentType(value);

                        try (InputStream is = Files.newInputStream(rasArtifact); 
                                OutputStream os = Files.newOutputStream(saArtifact, StandardOpenOption.CREATE_NEW, new SetContentType(type))) {
                            IOUtils.copy(is, os);
                        }
                    } catch(Exception e) {
                        logger.warn("Failed to copy run " + runName + " artifact " + key,e);
                    }
                }
            } catch(Exception e) {
                logger.warn("Failed to copy run " + runName + " artifacts",e);
            }
        }
    }
}
