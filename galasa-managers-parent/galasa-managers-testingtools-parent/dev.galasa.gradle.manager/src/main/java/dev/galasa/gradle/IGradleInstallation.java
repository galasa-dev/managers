/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle;

import dev.galasa.framework.spi.ResourceUnavailableException;
import java.nio.file.Path;

/**
 * Represents a Gradle installation.
 * Note: Interacts with the installation (archives, directories), not gradle itself.
 * 
 * @author Matthew Chivers
 * 
 */
public interface IGradleInstallation {
    
    /**
     * Returns the Path to the downloaded archive file.
     * e.g. "/users/testuser/testdir/gradle-6.8.2-bin.zip"
     * 
     * @return  Path  - The downloaded archive file
     */
    Path retrieveArchive() throws GradleManagerException, ResourceUnavailableException;

    /**
     * Returns the name of the archive file.
     * e.g. "gradle-6.8.2-bin.zip"
     * 
     * @return  String  -   Name of the archive file
     */
    String getArchiveFilename() throws GradleManagerException;
    
    /**
     * Returns the location of the gradle binary
     * e.g. /users/testuser/testdir/gradle-6.8.2/bin/gradle
     * 
     * @return  String  -   Location of the gradle binary
     */
    String getGradleCommand() throws GradleManagerException;

    /**
     * Provides the "Gradle Home" directory location.
     * e.g. /users/testuser/testdir/.gradle/
     * 
     * @return String   -  Location of the gradle home directory
     */
    String getGradleHome();

}