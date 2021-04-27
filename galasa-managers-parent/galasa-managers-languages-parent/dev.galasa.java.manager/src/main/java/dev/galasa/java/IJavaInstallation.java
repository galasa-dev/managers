/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.java;

import java.nio.file.Path;

public interface IJavaInstallation {
    
    Path retrieveArchive() throws JavaManagerException;
    String getArchiveFilename() throws JavaManagerException;
    
    String getJavaCommand() throws JavaManagerException;
    
}
