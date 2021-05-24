/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.manager.ivt;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentials;
import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;

@Test
public class ZosManagerBatchIVT {
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  "PRIMARY")
    public IZosImage imagePrimary;
    
    @ZosBatch(imageTag = "PRIMARY")
    public IZosBatch batch;
    
    @BundleResources
    public IBundleResources resources;    
    
    @Test
    public void preFlightTests() throws Exception {
        assertThat(imagePrimary).isNotNull();
        assertThat(batch).isNotNull();
        assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        
        logger.info("All provisioned objects have been correctly initialised");
        
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
        logger.info("The Primary Credentials are being returned");
    }

    //@Test - currently causes an NPE #711
    public void submitJCLNoSteps() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/resources/jcl/noSteps.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	job.waitForJob();
    }
    
    @Test
    public void submitJCLDoNothing() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/resources/jcl/doNothing.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	int returnCode = job.waitForJob();
    	assertThat(returnCode).isEqualTo(0);
    }
 

}
