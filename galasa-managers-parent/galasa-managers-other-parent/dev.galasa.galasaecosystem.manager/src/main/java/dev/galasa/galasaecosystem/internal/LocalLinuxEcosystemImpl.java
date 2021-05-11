package dev.galasa.galasaecosystem.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.artifact.IBundleResources;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IsolationInstallation;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxManagerException;

public class LocalLinuxEcosystemImpl extends LocalEcosystemImpl {

    private final Log                        logger = LogFactory.getLog(getClass());

    private final static Pattern processPattern = Pattern.compile("^\\QPROCESS=\\E(\\d+)$", Pattern.MULTILINE);
    private final static Pattern runnamePattern = Pattern.compile("Allocated Run Name (\\w+) to this run");

    private final ILinuxImage linuxImage;

    private int internalRunNumber = 0;

    private Path scriptFile;

    private final ArrayList<Path> runFiles = new ArrayList<Path>();
    private final HashMap<Path, String> runNameFiles = new HashMap<>();

    public LocalLinuxEcosystemImpl(GalasaEcosystemManagerImpl manager, String tag,
            ILinuxImage linuxImage, IJavaInstallation javaInstallation, IsolationInstallation isolationInstallation) throws LinuxManagerException {
        super(manager, tag, javaInstallation, isolationInstallation);

        this.linuxImage = linuxImage;
    }

    @Override
    public void build() throws GalasaEcosystemManagerException {
        try {
            logger.debug("Building a Galasa Local Ecosystem on Linux image " + this.linuxImage.getImageID());

            Path home = this.linuxImage.getHome();
            Path runHome = this.linuxImage.getRunDirectory();
            build(runHome, home);

            // copy our script file for running galasa tests and servers
            this.scriptFile = getGalasaConfigDirectory().resolve("run.sh");
            IBundleResources bundleResources = getEcosystemManager().getArtifactManager().getBundleResources(getClass());

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("MAVEN_REPO", getMavenRepo().toString());
            parameters.put("MAVEN_LOCAL", "file:" + getMavenLocal().toString());
            parameters.put("MAVEN_VERSION", getMavenVersion());
            parameters.put("JAVA_CMD", getJavaInstallation().getJavaCommand());
            parameters.put("BOOT_JAR", getBootJar().toString());
            parameters.put("BOOTSTRAP", "file:" + getBootstrapFile().toString());
            InputStream is = bundleResources.retrieveSkeletonFile("local/run.sh", parameters);
            Files.copy(is, this.scriptFile);

            getCommandShell().issueCommand("chmod +x " + this.scriptFile.toString());
        } catch (Exception e) {
            throw new GalasaEcosystemManagerException("Problem building the Local Ecosystem on Linux", e); 
        }

    }

    @Override
    public void stop() {
        // At the moment, nothing to stop
    }

    @Override
    public void discard() {
        // Will let the Linux manager discard to home dir, but we need to save all the data from the remote ecosystem

        super.discard();
        
        for(Path consoleFile : this.runFiles) {
            saveConsoleLog(consoleFile);
        }

    }

    private void saveConsoleLog(Path consoleFile) {
        String runName = this.runNameFiles.get(consoleFile);

        if (!Files.exists(consoleFile)) {
            return;
        }

        if (runName == null) {
            runName = "unknown";
        }

        Path saRoot = getEcosystemManager().getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        Path saRunConsoleFile = saRoot.resolve("ecosystem").resolve("runs").resolve(runName).resolve(consoleFile.getFileName().toString());

        try {
            Files.copy(consoleFile, saRunConsoleFile);
        } catch (IOException e) {
            logger.warn("Failed to copy the console log from " + consoleFile.toString());
        }
    }



    @Override
    public String submitRun(String runType, String requestor, String groupName, @NotNull String bundleName,
            @NotNull String testName, String mavenRepository, String obr, String stream, Properties overrides)
                    throws GalasaEcosystemManagerException {
        try {
            this.internalRunNumber++;

            Path overridesFile = null;

            // Create the overrides file if provided
            if (overrides != null && !overrides.isEmpty()) {
                overridesFile = this.linuxImage.getRunDirectory().resolve("run" + this.internalRunNumber + ".overrides");
                overrides.store(Files.newOutputStream(overridesFile, StandardOpenOption.CREATE_NEW), "Galasa Ecosystem Manager");
                this.runFiles.add(overridesFile);
            }

            // allocate the console log file

            Path consoleFile = this.linuxImage.getRunDirectory().resolve("run" + this.internalRunNumber + ".console");
            this.runFiles.add(consoleFile);


            StringBuilder runCommand = new StringBuilder();
            runCommand.append(this.scriptFile.toString());
            runCommand.append(" ");
            runCommand.append(consoleFile.toString());
            runCommand.append(" \"");
            if (overridesFile != null) {
                runCommand.append(" --overrides ");
                runCommand.append(overridesFile.toString());
            }
            runCommand.append(" --test ");
            runCommand.append(bundleName);
            runCommand.append("/");
            runCommand.append(testName);
            runCommand.append("\"");

            String response = getCommandShell().issueCommand(runCommand.toString());

            Matcher matcher = processPattern.matcher(response);
            if (!matcher.find()) {
                throw new GalasaEcosystemManagerException("Unexpected response for the run.sh script:-\n" + response);
            }

            int processNumber = Integer.parseInt(matcher.group(1));
            String runName = null;
            Instant expire = Instant.now().plus(2, ChronoUnit.MINUTES);
            while(expire.isAfter(Instant.now())) {
                Thread.sleep(2000);

                String consoleContents = new String(Files.readAllBytes(consoleFile), StandardCharsets.UTF_8);
                matcher = runnamePattern.matcher(consoleContents);
                if (matcher.find()) {
                    runName = matcher.group(1);
                    break;
                }
            }
            if (runName == null) {
                saveConsoleLog(consoleFile);
                this.runFiles.remove(consoleFile);
                throw new GalasaEcosystemManagerException("Unable to locate the assigned run name to the submitted run");
            }

            LocalRun localRun = new LocalRun(bundleName, testName, groupName, runName, processNumber, consoleFile, overridesFile);
            addLocalRun(localRun);
            this.runNameFiles.put(consoleFile, runName);
            if (overridesFile != null) {
                this.runNameFiles.put(overridesFile, runName);
            }

            logger.info("Submitted test run with run name of " + runName);

            return runName;
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Failed to submit run to local ecosystem", e);
        }

    }

    @Override
    protected ICommandShell getCommandShell() throws GalasaEcosystemManagerException {
        try {
            return this.linuxImage.getCommandShell();
        } catch (LinuxManagerException e) {
            throw new GalasaEcosystemManagerException("Problem obtaining command shell", e);
        }
    }

}
