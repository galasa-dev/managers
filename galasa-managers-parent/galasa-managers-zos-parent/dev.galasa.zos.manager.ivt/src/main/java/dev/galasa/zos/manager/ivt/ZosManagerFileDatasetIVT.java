/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zos.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentialsUsernamePassword;
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
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileHandler;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommand;

@Test
public class ZosManagerFileDatasetIVT {
    
    private final String IMG_TAG = "PRIMARY";
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  IMG_TAG)
    public IZosImage imagePrimary;
        
    @ZosFileHandler
    public IZosFileHandler fileHandler;
    
    @ZosBatch(imageTag = "PRIMARY")
    public IZosBatch batch;

    @BundleResources
    public IBundleResources resources; 
    
    @CoreManager
    public ICoreManager coreManager;
    
    @ZosUNIXCommand(imageTag = IMG_TAG)
    public IZosUNIXCommand zosUNIXCommand;
    
    private String userName = new String();
    private String runName  = new String();

    @Test
    public void preFlightTests() throws Exception {
        // Ensure we have the resources we need for testing
        assertThat(imagePrimary).isNotNull();
        assertThat(fileHandler).isNotNull();
        assertThat(coreManager).isNotNull();
        assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
        userName = ((ICredentialsUsernamePassword)imagePrimary.getDefaultCredentials()).getUsername().toLowerCase();
        runName = coreManager.getRunName().toUpperCase();
    }
    
   private boolean checkThatPDSExists(String dataset) throws TestBundleResourceException, IOException, ZosBatchException {
	   HashMap<String,Object> parms = new HashMap<>();
	   parms.put("DATASET", dataset);
	   String jcl = resources.retrieveSkeletonFileAsString("/resources/jcl/PDSCheck.jcl", parms);
	   IZosBatchJob job = batch.submitJob(jcl, null);
	   job.setShouldArchive(true);
	   job.setShouldCleanup(true);
	   int rc = job.waitForJob();
	   if(rc == 0)
		   return true;
	   else
		   return false;
   }
   
   @Test
   public void testExistingDS() throws ZosBatchException, TestBundleResourceException, IOException, ZosDatasetException {
	   String desiredDataSetName = "CTS.CICSCOG.JCL";
	   assertThat(checkThatPDSExists(desiredDataSetName)).isTrue();
	   assertThat(fileHandler.newDataset(desiredDataSetName, imagePrimary).exists()).isTrue();
   }
   
   @Test
   public void testNonExistingDS() throws ZosBatchException, TestBundleResourceException, IOException, ZosDatasetException {
	   String desiredDataSetName = "CTS.CICSCOG.JCJ";
	   assertThat(checkThatPDSExists(desiredDataSetName)).isFalse();
	   assertThat(fileHandler.newDataset(desiredDataSetName, imagePrimary).exists()).isFalse();
   }
    
   @Test
   public void testPDSCreate() throws Exception {
	   String desiredDataSetName = "CTS.GALASA." + runName;
	   assertThat(checkThatPDSExists(desiredDataSetName)).isFalse();
	   logger.info("Checked that " + desiredDataSetName + " doesn't currently exist");
	   
	   IZosDataset ds = fileHandler.newDataset(desiredDataSetName, imagePrimary);
	   ds.setDatasetOrganization(DatasetOrganization.SEQUENTIAL);
	   ds.setRecordFormat(RecordFormat.FIXED_BLOCKED);
	   ds.setRecordlength(80);
	   ds.setBlockSize(32720);
	   ds.setUnit("SYSDA");
	   ds.setSpace(SpaceUnit.TRACKS, 1, 1);
	   ds.create();
	   
	   assertThat(checkThatPDSExists(desiredDataSetName)).isTrue();
	   logger.info("Checked that " + desiredDataSetName + " now exists");
	   
	   ds.delete();
	   
	   assertThat(checkThatPDSExists(desiredDataSetName)).isFalse();
	   logger.info("Checked that " + desiredDataSetName + " has been deleted");
   }
}
