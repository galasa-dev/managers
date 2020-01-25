/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import org.apache.http.HttpStatus;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.framework.spi.ras.ResultArchiveStorePath;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchJobImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchJobOutputImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchJobnameImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchManagerImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.RestrictToImage;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.UseSysaff;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosBatchManagerImpl.class, JobWaitTimeout.class, UseSysaff.class, RestrictToImage.class})
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
    private ResultArchiveStorePath resultArchiveStorePathMock;

    private static final String FIXED_JOBNAME = "GAL45678";
    
    private static final String FIXED_JOBID = "JOB12345";

    private static final String FIXED_STATUS_OUTPUT = "OUTPUT";

    private static final String FIXED_RETCODE_0000 = "CC 0000";

    private static final String FIXED_RETCODE_0020 = "CC 0020";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setup() throws Exception {

        Path archivePath = Mockito.mock(Path.class);
        FileSystem mockFileSystem = Mockito.mock(FileSystem.class);
        FileSystemProvider mockFileSystemProvider = Mockito.mock(FileSystemProvider.class);
        OutputStream mockOutputStream = Mockito.mock(OutputStream.class);
        Mockito.when(archivePath.resolve(Mockito.anyString())).thenReturn(archivePath);
        Mockito.when(archivePath.getFileSystem()).thenReturn(mockFileSystem);
        Mockito.when(mockFileSystem.provider()).thenReturn(mockFileSystemProvider);
        SeekableByteChannel mockSeekableByteChannel = Mockito.mock(SeekableByteChannel.class);
        Mockito.when(mockFileSystemProvider.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mockSeekableByteChannel);
        Mockito.when(mockFileSystemProvider.newOutputStream(Mockito.any(Path.class), Mockito.any())).thenReturn(mockOutputStream);
        Mockito.when(mockFileSystem.getPath(Mockito.anyString(), Mockito.any())).thenReturn(archivePath);        
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
        
        PowerMockito.mockStatic(JobWaitTimeout.class);
        Mockito.when(JobWaitTimeout.get(Mockito.any())).thenReturn(2);
        
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
    
    @Test
    public void testConstructorJobWaitTimeoutException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Unable to get job timeout property value");
        Mockito.when(JobWaitTimeout.get(Mockito.anyString())).thenThrow(new ZosBatchManagerException("exception"));
        
        new ZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL");
    }
    
    @Test
    public void testConstructorUseSysaffException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Unable to get use SYSAFF property value");
        Mockito.when(UseSysaff.get(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));
        
        new ZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL");
    }
    
    @Test
    public void testConstructorRestrictToImageException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(RestrictToImage.get(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));
        
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
    
    @Test
    public void testSubmitJobIZosmfRestApiProcessorSendRequestException() throws ZosBatchException, ZosmfException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new ZosmfException("exception"));
        
        zosBatchJob.submitJob();
    }
    
    @Test
    public void testSubmitIZosmfResponseGetJsonContentException() throws ZosBatchException, ZosmfException {        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        zosBatchJob.submitJob();
    }
    
    @Test
    public void testSubmitJobNotStatusCodeCreated() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Submit job, category:0, rc:0, reason:0, message:message"));

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

    @Test
    public void testWaitForJobNotSubmittedException() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Job has not been submitted by manager");
        zosBatchJob.waitForJob();
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
        
        Assert.assertEquals("retrieveOutput() should return FIXED_JOBNAME_FIXED_JOBID", FIXED_JOBNAME + "_" + FIXED_JOBID, zosBatchJobSpy.retrieveOutput().toString());
    }
    
    @Test
    public void testRetrieveOutputNotSubmittedException() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Job has not been submitted by manager");
        zosBatchJob.retrieveOutput();
    }

    
    @Test
    public void testRetrieveOutputErrorResponse() throws Exception {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Mockito.doNothing().when(zosBatchJobSpy).addOutputFileContent(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(zosBatchJobSpy).archiveJobOutput();
        Mockito.doNothing().when(zosBatchJobSpy).purgeJob();
        Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Retrieve job output, category:0, rc:0, reason:0, message:message"));
        
        zosBatchJobSpy.retrieveOutput();
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
    public void testPurgeJobErrorResponse() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Purge job, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.purgeJob();
    }

    @Test
    public void testUpdateJobStatus() throws ZosBatchException, ZosmfException  {
        String nullString = null;
        Whitebox.setInternalState(zosBatchJobSpy, "status", nullString);
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", nullString);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);
        JsonObject jsonObject = getJsonObject();
        jsonObject.remove("status");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.jobComplete());
        

        Whitebox.setInternalState(zosBatchJobSpy, "status", nullString);
        jsonObject.addProperty("status", "STATUS");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.jobComplete());

        
        Whitebox.setInternalState(zosBatchJobSpy, "status", nullString);
        jsonObject.addProperty("status", "OUTPUT");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertTrue("jobComplete should be true", zosBatchJobSpy.jobComplete());
        

        Whitebox.setInternalState(zosBatchJobSpy, "retcode", nullString);
        jsonObject.remove("retcode");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertEquals("retcode should be ????", "????", zosBatchJobSpy.getRetcode());
    }

    @Test
    public void testUpdateJobStatusJobNotSubmitted() throws ZosBatchException, ZosmfException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Job has not been submitted by manager");

        zosBatchJobSpy.updateJobStatus();
    }
     
    @Test
    public void testUpdateJobStatusErrorResponse() throws ZosBatchException, ZosmfException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Update job status, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.updateJobStatus();
    }
    
    @Test
    public void testAchiveJobOutput() throws ZosBatchException, ZosmfException {
        Assert.assertEquals("isArchived() should return the false", false, zosBatchJobSpy.isArchived());
        
        PowerMockito.doReturn(null).when(zosBatchJobSpy).retrieveOutput();
        zosBatchJobSpy.archiveJobOutput();
        
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
        zosBatchJobSpy.addOutputFileContent(getJsonObject(), "path");
        
        Assert.assertEquals("Dummy assert", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
    }
    
    @Test
    public void testAddOutputFileContentErrorResponse() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Retrieve job output, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.addOutputFileContent(null, null);
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
    
    @Test 
    public void testStoreArtifactException() throws ZosBatchException {       
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Unabe to get archive path");
        ZosBatchManagerImpl.setArchivePath(null);
        
        zosBatchJobSpy.storeArtifact("content", "artifactPathElements");
    }
    
    @Test
    public void testBuildErrorString() {
        String expectedString = "Error action";
        String returnString = zosBatchJobSpy.buildErrorString("action", new JsonObject());
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        JsonObject jsonObject = getJsonObject();
        jsonObject.addProperty("details", "details");
        expectedString = "Error action, category:0, rc:0, reason:0, message:message\n" + 
                "details:details\n" + 
                "stack:\n" + 
                "stack";
        returnString = zosBatchJobSpy.buildErrorString("action", jsonObject);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        jsonObject.remove("details");
        JsonArray jsonArray = new JsonArray();
        JsonPrimitive item = new JsonPrimitive("details line 1");
        jsonArray.add(item);
        item = new JsonPrimitive("details line 2");
        jsonArray.add(item);
        jsonObject.add("details", jsonArray);
        expectedString = "Error action, category:0, rc:0, reason:0, message:message\n" + 
                "details:\n" +
                "details line 1\n" +
                "details line 2\n" + 
                "stack:\n" + 
                "stack";
        returnString = zosBatchJobSpy.buildErrorString("action", jsonObject);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
    }
    
    @Test
    public void testJosnNull() {
        JsonObject JsonObject = new JsonObject();
        Assert.assertNull("jsonNull() should return null", zosBatchJob.jsonNull(JsonObject, "none"));

        JsonObject.add("empty", JsonNull.INSTANCE);
        Assert.assertNull("jsonNull() should return null", zosBatchJob.jsonNull(JsonObject, "empty"));

        JsonObject.addProperty("property", "value");
        Assert.assertEquals("jsonNull() should return value", "value", zosBatchJob.jsonNull(JsonObject, "property"));
    }
    
    @Test
    public void testToString() throws ZosBatchException {        
        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Whitebox.setInternalState(zosBatchJobSpy, "jobid", "#JOBID#");
        Whitebox.setInternalState(zosBatchJobSpy, "status", "#STATUS#");
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", "#RETCODE#");
        String expectedString = "JOBID=#JOBID# JOBNAME=" + FIXED_JOBNAME + " STATUS=#STATUS# RETCODE=#RETCODE#";
        Assert.assertEquals("toString() should return supplied value", expectedString, zosBatchJobSpy.toString());
        
        PowerMockito.doThrow(new ZosBatchException("exception")).when(zosBatchJobSpy).updateJobStatus();
        zosBatchJobSpy.toString();
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
