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

import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
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
        runName = coreManager.getRunName();
        logger.info("Using Run ID of: " + runName);
    }
    
    /**
     * Run some JCL to check that a PDS has been created
     * This is used to test that zosFile is working correctly
     * @param dataset The name of the dataset that we want to test
     * @return a boolean to signify that the dataset exists (or not)
     * @throws TestBundleResourceException
     * @throws IOException
     * @throws ZosBatchException
     */
    private boolean checkThatPDSExists(String dataset) throws TestBundleResourceException, IOException, ZosBatchException {
    	HashMap<String,Object> parms = new HashMap<>();
    	parms.put("DATASET", dataset);
    	String jcl = resources.retrieveSkeletonFileAsString("/resources/jcl/PDSCheck.jcl", parms);
    	return(batch.submitJob(jcl, null).waitForJob() == 0);
    }
    
    /**
     * Run a piece of JCL to echo the content of the member in the PDS requested
     * Check that the strings provided as content exist within the content of the PDS member
     * @param pds The data set name 
     * @param member the member within the dataset
     * @param content lines of content to check
     * @return a boolean true if the content exists in the pds member, false if something is missing or the member does not exist
     * @throws ZosBatchException
     * @throws TestBundleResourceException
     * @throws IOException
     */
    private boolean checkContentWasWritten(String pds, String member, String... content) throws ZosBatchException, TestBundleResourceException, IOException {
    	String fullName = pds+"("+member+")";
    	HashMap<String, Object> parms = new HashMap<>();
    	parms.put("MEMBER_NAME",fullName);
    	IZosBatchJob job = batch.submitJob(resources.retrieveSkeletonFileAsString("/resources/jcl/list.jcl", parms),null);
    	job.setShouldArchive(false);
    	if(job.waitForJob() > 0)
    		return false;
    	IZosBatchJobOutputSpoolFile output = job.getSpoolFile("SYSUT2");
    	for(String c : content) {
    		if(!output.getRecords().contains(c))
    			return false;
    	}
    	return true;
    }
   
    //Test that an existing PDS exists
    @Test
    public void testExistingDS() throws ZosBatchException, TestBundleResourceException, IOException, ZosDatasetException {
    	String desiredDataSetName = "CTS.CICSCOG.JCL";
    	assertThat(checkThatPDSExists(desiredDataSetName)).isTrue();
    	assertThat(fileHandler.newDataset(desiredDataSetName, imagePrimary).exists()).isTrue();
    }
   
    //Test that a non-existant PDS doesn't exist 
    @Test
    public void testNonExistingDS() throws ZosBatchException, TestBundleResourceException, IOException, ZosDatasetException {
    	String desiredDataSetName = "CTS.CICSCOG.JCJ";
    	assertThat(checkThatPDSExists(desiredDataSetName)).isFalse();
    	assertThat(fileHandler.newDataset(desiredDataSetName, imagePrimary).exists()).isFalse();
    }
   
    private IZosDataset createBasicDataset(String name, boolean pds) throws ZosDatasetException {
    	IZosDataset ds = fileHandler.newDataset(name, imagePrimary);
    	if(pds) {
    		ds.setDirectoryBlocks(10);
    		ds.setDatasetOrganization(DatasetOrganization.PARTITIONED);
    	}	
    	else {
    		ds.setDatasetOrganization(DatasetOrganization.SEQUENTIAL);
    	}
    	ds.setRecordFormat(RecordFormat.FIXED_BLOCKED);
    	ds.setRecordlength(80);
    	ds.setBlockSize(32720);
    	ds.setUnit("SYSDA");
    	ds.setSpace(SpaceUnit.TRACKS, 1, 1);
    	ds.create();
    	return ds;
    }

    //Test that the manager can create a new PDS that doesn't exist and delete it
    //ensure that we always confirm that actions really have taken place
    @Test
    public void testPDSCreate() throws Exception {
    	String desiredDataSetName = "CTS.GALASA." + runName;
    	assertThat(checkThatPDSExists(desiredDataSetName)).isFalse();
    	logger.info("Checked that " + desiredDataSetName + " doesn't currently exist");
	   
    	IZosDataset ds = createBasicDataset(desiredDataSetName,false);
    	
    	assertThat(checkThatPDSExists(desiredDataSetName)).isTrue();
    	assertThat(ds.exists()).isTrue();
    	logger.info("Checked that " + desiredDataSetName + " now exists");
	   
    	ds.delete();
	   
    	assertThat(checkThatPDSExists(desiredDataSetName)).isFalse();
    	logger.info("Checked that " + desiredDataSetName + " has been deleted");
    }
   
    @Test
    public void datasetAttributeCheck() throws ZosBatchException, TestBundleResourceException, IOException, ZosDatasetException {
    	String desiredDataSetName = "CTS.GALASA." + runName;
    	assertThat(checkThatPDSExists(desiredDataSetName)).isFalse();
    	logger.info("Checked that " + desiredDataSetName + " doesn't currently exist");
	   
    	createBasicDataset(desiredDataSetName,false);
	   
    	IZosDataset ds = fileHandler.newDataset(desiredDataSetName, imagePrimary);
    	assertThat(ds.exists()).isTrue();
    	
    	ds.retrieveAttibutes();
    	assertThat(ds.getDatasetOrganization()).isEqualTo(DatasetOrganization.SEQUENTIAL);
    	assertThat(ds.getRecordFormat()).isEqualTo(RecordFormat.FIXED_BLOCKED);
    	assertThat(ds.getRecordlength()).isEqualTo(80);
    	//assertThat(ds.getBlockSize()).isEqualTo(32780); //32720
    	//assertThat(ds.getUnit()).isEqualTo("SYSDA"); //3390
    	assertThat(ds.getSpaceUnit()).isEqualTo(SpaceUnit.TRACKS);
    	
    	ds.delete();
	   
    }	
   
    @Test
   	public void testPDSMemberCreate() throws Exception {
    	String desiredDataSetName = "CTS.GALASA." + runName;
    	String memberName = "HOBBIT";
    	String hidingMember = "DRAGON";
    	String content = "Basic PDS Member test";
    	String content2 = "a second line of content";
    	IZosDataset ds = createBasicDataset(desiredDataSetName,true);
    	assertThat(ds.memberList().size()).isEqualTo(0);
    	ds.memberCreate(memberName);
    	assertThat(ds.memberExists(memberName)).isTrue();
	   
    	ds.memberStoreText(memberName, content+"\n"+content2);
	   
    	//check through JCL that we wrote what we thought we wrote
    	assertThat(checkContentWasWritten(desiredDataSetName, memberName, content, content2));
	   
    	String result = ds.memberRetrieveAsText(memberName).trim();
    	assertThat(result).startsWith(content);
    	assertThat(result).endsWith(content2);
    	
    	assertThat(ds.memberList().size()).isEqualTo(1);
    	assertThat(ds.memberList()).contains(memberName);
    	
    	//before we delete and clean up - delete a member that doesn't exist
    	//we should still have 1 item in the PDS
    	ds.memberDelete(hidingMember);
    	assertThat(ds.memberList().size()).isEqualTo(1);
	   
    	ds.memberSaveToResultsArchive(memberName, "PDSOutput");
    	ds.memberDelete(memberName);
    	assertThat(ds.memberExists(memberName)).isFalse();
    	assertThat(ds.memberList().size()).isEqualTo(0);
    	
    	ds.delete();
    }
    
    @Test
    public void deleteNonExistingMember() throws ZosDatasetException {
    	String desiredDataSetName = "CTS.GALASA." + runName;
    	String memberName = "HOBBIT";
    	String hidingMember = "DRAGON";
    	IZosDataset ds = createBasicDataset(desiredDataSetName,true);
    	assertThat(ds.memberList().size()).isEqualTo(0);
    	ds.memberCreate(memberName);
    	assertThat(ds.memberExists(memberName)).isTrue();
    	//this doesn't exist - but also shouldn't
    	ds.memberDelete(hidingMember);
    	
    }
}
