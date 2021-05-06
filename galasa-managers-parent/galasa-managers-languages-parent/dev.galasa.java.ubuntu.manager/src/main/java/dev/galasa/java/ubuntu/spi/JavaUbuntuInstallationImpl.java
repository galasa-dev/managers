/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.java.ubuntu.spi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.CpuArchitecture;
import dev.galasa.OperatingSystem;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.JavaType;
import dev.galasa.java.JavaVersion;
import dev.galasa.java.spi.JavaInstallationImpl;
import dev.galasa.java.ubuntu.IJavaUbuntuInstallation;
import dev.galasa.java.ubuntu.JavaUbuntuManagerException;
import dev.galasa.java.ubuntu.internal.JavaUbuntuManagerImpl;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.spi.ILinuxManagerSpi;

public class JavaUbuntuInstallationImpl extends JavaInstallationImpl implements IJavaUbuntuInstallation {

    private final static Log                   logger       = LogFactory.getLog(JavaUbuntuInstallationImpl.class);

    private final JavaUbuntuManagerImpl javaUbuntuManager;
    private final String imageTag;

    private final String javaDirectoryName;
    private Path javaHome;

    private static final Pattern findHome = Pattern.compile("^(([a-zA-Z0-9\\-\\+\\._/ \\\\]*)?/)?\\Qbin/java\\E$", Pattern.MULTILINE);



    public JavaUbuntuInstallationImpl(JavaUbuntuManagerImpl javaUbuntuManager, 
            JavaType javaType, 
            JavaVersion javaVersion, 
            String javaJvm, 
            String javaTag, 
            String imageTag) throws JavaManagerException {
        super(javaUbuntuManager.getJavaManager(),
                javaType,
                OperatingSystem.linux,
                CpuArchitecture.x64,
                javaVersion, 
                javaJvm, 
                javaTag);

        this.javaUbuntuManager = javaUbuntuManager;
        this.imageTag          = imageTag;

        this.javaDirectoryName = javaType.name() 
                + "_" 
                + javaVersion.name()
                + "_"
                + javaJvm;
    }

    public void build() throws JavaManagerException {

        String actualFilename = getArchiveFilename();

        try {
            ILinuxManagerSpi linuxManager = this.javaUbuntuManager.getLinuxManager();

            ILinuxImage image = linuxManager.getImageForTag(imageTag);

            Path home = image.getHome();
            Path runhome = image.getRunDirectory();

            Path managerHome  = home.resolve(JavaUbuntuManagerImpl.NAMESPACE);
            Path javasHome    = managerHome.resolve("javas");
            Path archivesHome = managerHome.resolve("archives");         


            // for DSE and speed reasons, check to see if we need to install the 
            // Java installation.

            if (Files.exists(javasHome)) {
                Path possibleJavaHome = javasHome.resolve(this.javaDirectoryName);
                if (Files.exists(possibleJavaHome)) {
                    this.javaHome = possibleJavaHome;
                    logger.info("Java installation for tag '" + getTag() + "' is preinstalled at " + this.javaHome);
                    return;
                }
            }


            // not preinstalled,  so check if the archive is available
            Path remoteArchive = null;

            if (Files.exists(archivesHome)) {
                Path possibleArchive = archivesHome.resolve(actualFilename);
                if (Files.exists(possibleArchive)) {
                    remoteArchive = possibleArchive;
                    logger.trace("Java archive for tag '" + getTag() + "' is located at " + possibleArchive);
                }
            }

            // Not installed and no archive,  need to download
            if (remoteArchive == null) {
                Path localArchive = retrieveArchive();
                logger.trace("Java archive for tag '" + getTag() + "' retrieved");

                // Make sure runhome is there, // TODO move to the linux manager
                if (!Files.exists(runhome)) {
                    Files.createDirectory(runhome);
                }


                remoteArchive = runhome.resolve(actualFilename);
                Files.copy(localArchive, remoteArchive);
                logger.trace("Java archive for tag '" + getTag() + "' has been transferred to the linux image");
            }


            // Install it into the run directory
            Path possibleJavaHomes = runhome.resolve(JavaUbuntuManagerImpl.NAMESPACE + "/javas");
            Files.createDirectories(possibleJavaHomes);
            Path tempExtractDirectory = runhome.resolve(JavaUbuntuManagerImpl.NAMESPACE + "/extract/" + this.javaDirectoryName);
            Files.createDirectories(tempExtractDirectory);


            ICommandShell commandShell = image.getCommandShell();
            if (actualFilename.endsWith(".tgz") || actualFilename.endsWith(".tar.gz")) {
                String response = commandShell.issueCommand("cd " + tempExtractDirectory + ";tar -xvzf " + remoteArchive + ";echo response=$?");

                if (!response.contains("response=0")) {
                    throw new JavaManagerException("Extract of the Java archive failed\n" + response);
                }


                // Check to see if the java home directory is in a sub folder of the archive
                Matcher matcher = findHome.matcher(response);
                if (!matcher.find()) {
                    throw new JavaManagerException("Unable to locate Java home in the archive");
                }

                Path targetHome = possibleJavaHomes.resolve(this.javaDirectoryName);
                Path extractedHome = tempExtractDirectory;

                String prefixDirectories = matcher.group(2);
                if (prefixDirectories != null && !prefixDirectories.isEmpty()) {
                    if ("/".equals(prefixDirectories)) {
                        throw new JavaManagerException("Unexpected found Java home in the root directory of the archive, ie prefixed /");
                    }
                    extractedHome = tempExtractDirectory.resolve(prefixDirectories);
                }

                response = commandShell.issueCommand("mv " + extractedHome + " " + targetHome + ";echo response=$?");
                if (!response.contains("response=0")) {
                    throw new JavaManagerException("Move of the extracted Java home failed\n" + response);
                }

                this.javaHome = targetHome;
                logger.info("Java installation for tag '" + getTag() + "' has been installed at " + this.javaHome);
            } else {
                throw new JavaUbuntuManagerException("Unable to support uncompressing of archive, unknown type");
            }

        } catch(Exception e) {
            throw new JavaUbuntuManagerException("Problem installing Java on server", e);
        }
    }

    public void discard() {
        super.discard();
    }

    @Override
    public String getJavaCommand() throws JavaManagerException {
        return this.javaHome.resolve("bin/java").toString();
    }

}
