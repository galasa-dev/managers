/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.java;

import java.nio.file.Path;

import dev.galasa.framework.spi.ResourceUnavailableException;

public interface IJavaInstallation {
    
    Path retrieveArchive() throws JavaManagerException, ResourceUnavailableException;
    Path retrieveJacocoAgent() throws JavaManagerException, ResourceUnavailableException;
    String getArchiveFilename() throws JavaManagerException;
    
    String getJavaCommand() throws JavaManagerException;

    /**
     * Provides the "Java Home" directory location within the unpacked java archive.
     * @return String 
     */
    String getJavaHome();

}
