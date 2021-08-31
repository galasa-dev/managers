/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle;

import dev.galasa.framework.spi.ResourceUnavailableException;
import java.nio.file.Path;

/**
 * Represents a Gradle installation, providing utility methods that can interact with the installation.
 * Note: Interacts with the installation (archives, directories) not gradle itself.
 * 
 * @author Matthew Chivers
 * 
 */
public interface IGradleInstallation {
    
    Path retrieveArchive() throws GradleManagerException, ResourceUnavailableException;

    String getArchiveFilename() throws GradleManagerException;
    
    String getGradleCommand() throws GradleManagerException;

    /**
     * Provides the "Gradle Home" directory location within the unpacked Gradle archive.
     * @return String 
     */
    String getGradleHome();

}