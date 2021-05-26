/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.manager.ivt;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileHandler;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

@Test
public class ZosManagerFileIVT {
	
	private final String IMG_TAG = "PRIMARY";
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  IMG_TAG)
    public IZosImage imagePrimary;
        
	@ZosFileHandler
    public IZosFileHandler fileHandler;

    @BundleResources
    public IBundleResources resources; 
    
    @CoreManager
    public ICoreManager coreManager;
    
    @ZosUNIXCommand(imageTag = IMG_TAG)
    public IZosUNIXCommand zosUNIXCommand;

    @Test
    public void preFlightTests() throws Exception {
    	// Ensure we have the resources we need for testing
        assertThat(imagePrimary).isNotNull();
        assertThat(fileHandler).isNotNull();
        assertThat(coreManager).isNotNull();
        assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
    }

}
