/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.core.manager.ivt;

import org.apache.commons.logging.Log;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.Summary;
import dev.galasa.Test;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.RunName;
import dev.galasa.core.manager.TestProperty;

@Test
@Summary("Ensure the basic functions are working in the Core Manager")
public class CoreManagerIVT {
    
    @Logger
    public Log logger;
    
    @RunName
    public String runName;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @TestProperty(prefix="random", suffix="ivt", infixes="property.core")
    public String prop;  
    
    
    @Test
    public void checkLogger() throws Exception {
        if (logger == null) {
            throw new Exception("Logger field is null, should have been filled by the Core Manager");
        }
        logger.info("The Logger field has been correctly initialised");
    }

    @Test
    public void checkRunName() throws Exception {
        if (runName == null || runName.trim().isEmpty()) {
            throw new Exception("Run Name field is null, should have been filled by the Core Manager");
        }
        logger.info("The Run Name  field has been correctly initialised = '" + runName + "'");
    }

    @Test
    public void checkCoreManager() throws Exception {
        if (coreManager == null) {
            throw new Exception("Core Manager field is null, should have been filled by the Core Manager");
        }
        logger.info("The Core Manager field has been correctly initialised");
    }

    @Test
    public void checkCreateTestProperty() {
    	assertThat(prop).isEqualTo("TEST");
    }
    
    
    @Test
    public void checkGetCredentials() throws CoreManagerException { 
    	ICredentialsUsernamePassword creds = (ICredentialsUsernamePassword) coreManager.getCredentials("SIMBANK");
    	assertThat(creds).isNotNull();
    	assertThat(creds.getUsername()).isEqualTo("IBMUSER");
    	assertThat(creds.getPassword()).isEqualTo("SYS1");
    }
    
    
    @Test
    public void testRegisterConfidentialtext() { // looking for a way to test it
    	
    	coreManager.registerConfidentialText("SYS1", "IBM user password");
    	
    }
    

}
