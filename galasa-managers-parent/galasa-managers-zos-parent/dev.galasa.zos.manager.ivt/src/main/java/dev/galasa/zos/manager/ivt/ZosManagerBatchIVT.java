/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.manager.ivt;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchJobname;

@Test
public class ZosManagerBatchIVT {
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  "PRIMARY")
    public IZosImage imagePrimary;
    
    @ZosBatch(imageTag = "PRIMARY")
    public IZosBatch batch;
    
    @ZosBatchJobname(imageTag = "PRIMARY")
    public IZosBatchJobname jobName;
    
    @BundleResources
    public IBundleResources resources;    
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Test
    public void preFlightTestsBasic() {
    	assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(coreManager).isNotNull();
    }
    @Test
    public void preFlightTestsZos() throws Exception {
        assertThat(imagePrimary).isNotNull();        
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
    }
    
    @Test
    public void preFlightTestsBatch() {
    	assertThat(batch).isNotNull();
        assertThat(jobName).isNotNull();
        assertThat(jobName.getName()).isUpperCase();
    }

    //@Test - currently causes an NPE #711
    public void submitJCLNoSteps() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/resources/jcl/noSteps.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	job.waitForJob();
    }
    
    /*
     * Basic submission and wait for a job 
     * The job submitted doesn't do anything of interest
     */
    @Test
    public void submitJCLDoNothing() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/resources/jcl/doNothing.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, null);
    	int returnCode = job.waitForJob();
    	assertThat(returnCode).isEqualTo(0);
    }
    
    /*
     * Submits the basic job but with a provisioned
     * job name - we perform an extra check that the job name
     * was used in the job
     */
    @Test
    public void submitJCLDoNothingJobName() throws TestBundleResourceException, IOException, ZosBatchException {
    	String jclInput = resources.retrieveFileAsString("/resources/jcl/doNothing.jcl");
    	IZosBatchJob job = batch.submitJob(jclInput, jobName);
    	int returnCode = job.waitForJob();
    	assertThat(job.getJobname().getName()).isEqualTo(jobName.getName());
    	assertThat(returnCode).isEqualTo(0);
    }
    


 

}
