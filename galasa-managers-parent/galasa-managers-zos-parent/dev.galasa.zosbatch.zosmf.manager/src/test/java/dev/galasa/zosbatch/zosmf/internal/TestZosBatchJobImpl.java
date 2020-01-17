/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.http.HttpStatus;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.zosmf.internal.properties.RestrictToImage;
import dev.galasa.zosbatch.zosmf.internal.properties.UseSysaff;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
// If coverage does not work in Eclipse with @RunWith(PowerMockRunner.class), then use the following:
//   @RunWith(org.mockito.junit.MockitoJUnitRunner.class)
// and add the following @Rule 
//   @Rule
//   public org.powermock.modules.junit4.rule.PowerMockRule rule = new org.powermock.modules.junit4.rule.PowerMockRule();
//   static {
//       org.powermock.modules.agent.PowerMockAgent.initializeIfNeeded();
//   }
// You may also need to add the following to Launch configuration VM args:
//    -noverify
// See http://www.notonlyanecmplace.com/make-eclemma-test-coverage-work-with-powermock/
@PrepareForTest({ZosBatchJobImpl.class, ZosBatchManagerImpl.class, JobWaitTimeout.class, UseSysaff.class, RestrictToImage.class})
public class TestZosBatchJobImpl {
	
	private ZosBatchJobImpl zosBatchJob;
	
	private ZosBatchJobImpl zosBatchJobSpy;

	@Mock
	private IZosImage zosImageMock;

	@Mock
	private ZosBatchJobnameImpl zosJobnameMock;

	@Mock
	private ZosmfManagerImpl zosmfManagerMock;
	
	@Mock
	private IZosmfRestApiProcessor zosmfApiProcessorMock;
	
	@Mock
	private IZosmfResponse zosmfResponseMockSubmit;
	
	@Mock
	private IZosmfResponse zosmfResponseMockStatus;
	
	@Mock
	private ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFileMock;
	
	@Mock
	private ZosBatchJobOutputImpl zosBatchJobOutputMock; 
	
	private static final String FIXED_JOBNAME = "GAL45678";
	
	private static final String FIXED_JOBID = "JOB12345";

	private static final String FIXED_STATUS_OUTPUT = "OUTPUT";

	private static final String FIXED_RETCODE_0000 = "CC 0000";

	private static final String FIXED_RETCODE_0020 = "CC 0020";

	@Rule
	public ExpectedException exceptionRule= ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		exceptionRule  = ExpectedException.none();
		
		Path archivePath = new File("dummy.path").toPath();
		PowerMockito.mockStatic(Files.class);
		Mockito.when(Files.createFile(Mockito.any(Path.class), Mockito.any())).thenReturn(archivePath);
		Mockito.when(Files.write(Mockito.any(), ArgumentMatchers.any(byte[].class))).thenReturn(archivePath);
		
		Mockito.when(zosImageMock.getImageID()).thenReturn("image");
		
		Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
		
		PowerMockito.mockStatic(JobWaitTimeout.class);
		Mockito.when(JobWaitTimeout.get(Mockito.any())).thenReturn(1);
		
		PowerMockito.mockStatic(UseSysaff.class);
		Mockito.when(UseSysaff.get(Mockito.any())).thenReturn(false);
		
		PowerMockito.mockStatic(RestrictToImage.class);
		Mockito.when(RestrictToImage.get(Mockito.any())).thenReturn(true);
		
		
		ZosBatchManagerImpl.setArchivePath(archivePath);
		ZosBatchManagerImpl.setCurrentTestMethod(TestZosBatchJobImpl.class.getDeclaredMethod("setup"));

		Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictToImage.get(zosImageMock.getImageID()))).thenReturn(zosmfApiProcessorMock);
		ZosBatchManagerImpl.setZosmfManager(zosmfManagerMock);
		
		zosBatchJob = new ZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL");
		zosBatchJobSpy = Mockito.spy(zosBatchJob);
	}
	
	@Test
	public void testConstructor() throws ZosBatchException {
		Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJob.getJobname().getName());
	}
	
	@Test(expected = ZosBatchManagerException.class)
	public void testConstructorJobWaitTimeoutException() throws ZosBatchManagerException {
		exceptionRule.expect(ZosBatchManagerException.class);
		Mockito.when(JobWaitTimeout.get(Mockito.anyString())).thenThrow(new ZosBatchManagerException("exception"));
		exceptionRule.expectMessage("Unable to get job timeout property value");
		
		new ZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL");
	}
	
	@Test(expected = ZosBatchManagerException.class)
	public void testConstructorUseSysaffException() throws ZosBatchManagerException {
		exceptionRule.expect(ZosBatchManagerException.class);
		Mockito.when(UseSysaff.get(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));
		exceptionRule.expectMessage("Unable to get use SYSAFF property value");
		
		new ZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL");
	}
	
	@Test(expected = ZosBatchManagerException.class)
	public void testConstructorRestrictToImageException() throws ZosBatchManagerException {
		exceptionRule.expect(ZosBatchManagerException.class);
		Mockito.when(RestrictToImage.get(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));
		exceptionRule.expectMessage("Unable to get use SYSAFF property value");
		
		new ZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL");
	}
	
	@Test
	public void testGetJobId() {
		Assert.assertEquals("getJobId() should return the 'unknown' value", "????????", zosBatchJob.getJobId());
		Whitebox.setInternalState(zosBatchJob, "jobid", FIXED_JOBID);
		Assert.assertEquals("getJobId() should return the supplied value", FIXED_JOBID, zosBatchJob.getJobId());
	}
	
	@Test
	public void testGetStatus() {
		Assert.assertEquals("getStatus() should return the 'unknown' value", "????????", zosBatchJob.getStatus());
		Whitebox.setInternalState(zosBatchJob, "status", FIXED_STATUS_OUTPUT);
		Assert.assertEquals("getStatus() should return the 'unknown' value", FIXED_STATUS_OUTPUT, zosBatchJob.getStatus());
	}
	
	@Test
	public void testGetRetcode() {
		Assert.assertEquals("getRetcode() should return the 'unknown' value", "????", zosBatchJob.getRetcode());
		Whitebox.setInternalState(zosBatchJob, "retcode", FIXED_RETCODE_0000);
		Assert.assertEquals("getRetcode() should return the supplied value", FIXED_RETCODE_0000, zosBatchJob.getRetcode());
	}
	
	@Test
	public void testSubmitJob() throws ZosBatchException, ZosmfException {
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockSubmit);
		Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
		Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
		
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);
		Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
		Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		
		zosBatchJob.submitJob();
		Assert.assertEquals("getJobname().getName() should return the supplied value", FIXED_JOBNAME, zosBatchJob.getJobname().getName());
	}
	
	@Test(expected = ZosBatchManagerException.class)
	public void testSubmitJobIZosmfRestApiProcessorSendRequestException() throws ZosBatchException, ZosmfException {
		exceptionRule.expect(ZosmfException.class);
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new ZosmfException("exception"));
		exceptionRule.expectMessage("exception");
		
		zosBatchJob.submitJob();
	}
	
	@Test(expected = ZosBatchException.class)
	public void testSubmitIZosmfResponseGetJsonContentException() throws ZosBatchException, ZosmfException {
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockSubmit);		
		exceptionRule.expect(ZosBatchException.class);
		Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenThrow(new ZosmfException("exception"));
		exceptionRule.expectMessage("exception");
		
		zosBatchJob.submitJob();
	}
	
	@Test(expected = ZosBatchException.class)
	public void testSubmitJobNotStatusCodeCreated() throws ZosBatchException, ZosmfException {
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockSubmit);
		Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
		Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		
		exceptionRule.expect(ZosBatchException.class);
		exceptionRule.expectMessage("Error Submit job, category:0, rc:0, reason:0, message:message\r\n" + 
				"stack:\r\n" + 
				"stack");
		zosBatchJob.submitJob();
	}
	
	@Test
	public void testWaitForJob() throws ZosBatchException, ZosmfException {
		Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
		
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);

		JsonObject responseBody = getJsonObject();
		Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(responseBody);
		Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		Assert.assertEquals("waitForJob() should return zero", 0, zosBatchJobSpy.waitForJob());

		responseBody = getJsonObject();
		responseBody.addProperty("retcode", FIXED_RETCODE_0020);
		Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(responseBody);
		Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		Assert.assertEquals("waitForJob() should return the supplied value", 20, zosBatchJobSpy.waitForJob());
		
		responseBody.addProperty("retcode", "CC XXXX");
		Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());

		responseBody.remove("retcode");
		Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());

		Mockito.doReturn(false).when(zosBatchJobSpy).jobComplete();
		Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());
	}
	
	@Test(expected = ZosBatchException.class)
	public void testWaitForJobNotSubmittedException() throws ZosBatchException {
		exceptionRule.expect(ZosBatchException.class);
		exceptionRule.expectMessage("Job has not been submitted by manager");
		zosBatchJob.waitForJob();
	}
	
	@Test(expected = ZosBatchException.class)
	public void testRetrieveOutputNotSubmittedException() throws ZosBatchException {
		exceptionRule.expect(ZosBatchException.class);
		exceptionRule.expectMessage("Job has not been submitted by manager");
		zosBatchJob.retrieveOutput();
	}
	
	@Test
	public void testRetrieveOutput() throws Exception {
		Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
		Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
		Mockito.doNothing().when(zosBatchJobSpy).addOutputFileContent(Mockito.any(), Mockito.any());
		Mockito.doNothing().when(zosBatchJobSpy).archiveJobOutput();
		Mockito.doNothing().when(zosBatchJobSpy).purgeJob();
		Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);

		Mockito.when(zosmfResponseMockStatus.getJsonArrayContent()).thenReturn(getJsonArray());
		Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		
		// When new ZosBatchJobOutputImpl return zosBatchJobOutputMock
		PowerMockito.whenNew(ZosBatchJobOutputImpl.class).withArguments(Mockito.anyString(), Mockito.anyString()).thenReturn(zosBatchJobOutputMock);
		
		Assert.assertEquals("retrieveOutput() should return the mocked ZosBatchJobOutputImpl", zosBatchJobOutputMock, zosBatchJobSpy.retrieveOutput());
	}
	
	@Test
	public void testPurgeJob() throws ZosBatchException, ZosmfException {
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);

		Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		Assert.assertEquals("isPurged() should return the false", false, zosBatchJobSpy.isPurged());
		zosBatchJobSpy.purgeJob();
		Assert.assertEquals("isPurged() should return the true", true, zosBatchJobSpy.isPurged());
		
		zosBatchJobSpy.purgeJob();
	}
	
	@Test
	public void testAchiveJobOutput() throws ZosBatchException, ZosmfException {
		Assert.assertEquals("isArchived() should return the false", false, zosBatchJobSpy.isArchived());
		ZosBatchJobOutputImpl zosBatchJobOutput = new ZosBatchJobOutputImpl(FIXED_JOBNAME, FIXED_JOBID);
		zosBatchJobOutput.addJcl("JCL");
		zosBatchJobOutput.add(getJsonObject(), "records");
		
		Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutput);
		Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
		Whitebox.setInternalState(zosBatchJobSpy, "jobname", zosJobnameMock);
		Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
		Whitebox.setInternalState(zosBatchJobSpy, "retcode", FIXED_RETCODE_0000);
		
		zosBatchJobSpy.archiveJobOutput();
		Assert.assertEquals("isArchived() should return the true", true, zosBatchJobSpy.isArchived());
		
		zosBatchJobSpy.archiveJobOutput();
	}
	
	@Test
	public void testAddOutputFileContent() throws ZosBatchException, ZosmfException {
		Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);
		Mockito.when(zosmfResponseMockStatus.getTextContent()).thenReturn("content");
		Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		ZosBatchJobOutputImpl zosBatchJobOutput = new ZosBatchJobOutputImpl(FIXED_JOBNAME, FIXED_JOBID);
		zosBatchJobOutput.addJcl("JCL");
		zosBatchJobOutput.add(getJsonObject(), "records");
		
		Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutput);
		
		zosBatchJobSpy.addOutputFileContent(null, null);
		Assert.assertEquals("Dummy assert", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
	}
	
	@Test
	public void testJclWithJobcard() throws ZosBatchManagerException {
		Whitebox.setInternalState(zosBatchJobSpy, "useSysaff", false);
		String jobWithJobcard = "//" + FIXED_JOBNAME + " JOB"; 
		Assert.assertThat("jclWithJobcard() should a return valid job card", zosBatchJobSpy.jclWithJobcard(), StringStartsWith.startsWith(jobWithJobcard));
		Whitebox.setInternalState(zosBatchJobSpy, "useSysaff", true);
		jobWithJobcard = jobWithJobcard + " \n/*JOBPARM SYSAFF="; 
		Assert.assertThat("jclWithJobcard() should return a valid job card with SYSAFF", zosBatchJobSpy.jclWithJobcard(), StringStartsWith.startsWith(jobWithJobcard));
		
	}
	
	private JsonObject getJsonObject() {
		JsonObject responseBody = new JsonObject();
		responseBody.addProperty("jobname", FIXED_JOBNAME);
		responseBody.addProperty("jobid", FIXED_JOBID);
		responseBody.addProperty("retcode", FIXED_RETCODE_0000);
		responseBody.addProperty("status", FIXED_STATUS_OUTPUT);
		responseBody.addProperty("category", 0);
		responseBody.addProperty("rc", 0);
		responseBody.addProperty("reason", 0);
		responseBody.addProperty("message", "message");
		responseBody.addProperty("stack", "stack");
		responseBody.addProperty("id", 1);
		responseBody.addProperty("ddname", "ddname");
		responseBody.addProperty("stepname", "stepname");
		responseBody.addProperty("procstep", "procstep");
		return responseBody;
	}
	
	private JsonArray getJsonArray() {
		JsonArray fileArray = new JsonArray();
		fileArray.add(getJsonObject());
		return fileArray;
	}
}
