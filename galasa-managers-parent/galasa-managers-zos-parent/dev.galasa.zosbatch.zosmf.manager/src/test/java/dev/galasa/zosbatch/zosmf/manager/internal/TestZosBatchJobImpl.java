/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import dev.galasa.zosbatch.zosmf.manager.internal.properties.TruncateJCLRecords;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.UseSysaff;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosBatchManagerImpl.class, JobWaitTimeout.class, UseSysaff.class, RestrictToImage.class, TruncateJCLRecords.class})
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
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
        Mockito.when(zosJobnameMock.getParams()).thenReturn("");
        
        PowerMockito.mockStatic(JobWaitTimeout.class);
        Mockito.when(JobWaitTimeout.get(Mockito.any())).thenReturn(2);
        
        PowerMockito.mockStatic(UseSysaff.class);
        Mockito.when(UseSysaff.get(Mockito.any())).thenReturn(false);
        
        PowerMockito.mockStatic(RestrictToImage.class);
        Mockito.when(RestrictToImage.get(Mockito.any())).thenReturn(true);
        
        PowerMockito.mockStatic(TruncateJCLRecords.class);
        Mockito.when(TruncateJCLRecords.get(Mockito.any())).thenReturn(true);        
        
        ZosBatchManagerImpl.setArchivePath(newMockedPath(false));
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
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosBatchJob.submitJob();
        Assert.assertEquals("getJobname().getName() should return the supplied value", FIXED_JOBNAME, zosBatchJob.getJobname().getName());
    }
    
    @Test
    public void testSubmitJobIZosmfRestApiProcessorSendRequestException() throws ZosBatchException, ZosmfException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        zosBatchJob.submitJob();
    }
    
    @Test
    public void testSubmitIZosmfResponseGetJsonContentException() throws ZosBatchException, ZosmfException {        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        zosBatchJob.submitJob();
    }
    
    @Test
    public void testSubmitJobNotStatusCodeCreated() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Submit job, category:0, rc:0, reason:0, message:message"));

        zosBatchJob.submitJob();
    }
    
    @Test
    public void testWaitForJob() throws ZosmfException, ZosBatchManagerException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        JsonObject responseBody = getJsonObject();
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(responseBody);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Assert.assertEquals("waitForJob() should return zero", 0, zosBatchJobSpy.waitForJob());

        responseBody = getJsonObject();
        responseBody.addProperty("retcode", FIXED_RETCODE_0020);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(responseBody);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Assert.assertEquals("waitForJob() should return the supplied value", 20, zosBatchJobSpy.waitForJob());
        
        responseBody.addProperty("retcode", "CC UNKNOWN");
        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());

        responseBody.remove("retcode");
        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());

        Mockito.doReturn(false).when(zosBatchJobSpy).isComplete();
        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());

        Mockito.when(JobWaitTimeout.get(Mockito.any())).thenReturn(1);
        Thread.currentThread().interrupt();
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("java.lang.InterruptedException");
        zosBatchJobSpy.waitForJob();
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
        Mockito.doNothing().when(zosBatchJobSpy).purge();
        Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

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
    public void testRetrieveOutputZosmfException() throws ZosBatchException, ZosmfException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Mockito.doNothing().when(zosBatchJobSpy).addOutputFileContent(Mockito.any(), Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchJobSpy.retrieveOutput();
    }

    @Test
    public void testRetrieveOutputZosmfResponseException() throws ZosBatchException, ZosmfException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Mockito.doNothing().when(zosBatchJobSpy).addOutputFileContent(Mockito.any(), Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
    
        Mockito.when(zosmfResponseMockStatus.getContent()).thenReturn(getJsonArray());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(zosmfResponseMockStatus.getJsonArrayContent()).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchJobSpy.retrieveOutput();
    }

    @Test
    public void testRetrieveOutputZosmfResponseException1() throws ZosBatchException, ZosmfException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Mockito.doNothing().when(zosBatchJobSpy).addOutputFileContent(Mockito.any(), Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
    
        Mockito.when(zosmfResponseMockStatus.getContent()).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchJobSpy.retrieveOutput();
    }

    @Test
    public void testRetrieveOutputBadHttpResponseException() throws Exception {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Mockito.doNothing().when(zosBatchJobSpy).addOutputFileContent(Mockito.any(), Mockito.any());
        Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
    
        Mockito.when(zosmfResponseMockStatus.getContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Retrieve job output, category:0, rc:0, reason:0, message:message"));
        
        zosBatchJobSpy.retrieveOutput();
    }

    @Test
    public void testCancelJob() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        Assert.assertEquals("isComplete() should return the false", false, zosBatchJobSpy.isComplete());
        zosBatchJobSpy.cancel();
        Assert.assertEquals("isComplete() should return the true", true, zosBatchJobSpy.isComplete());
        
        zosBatchJobSpy.cancel();
    }

    @Test
    public void testCancelZosmfException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");

        zosBatchJobSpy.cancel();
    }

    @Test
    public void testCancelResponseException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");

        zosBatchJobSpy.cancel();
    }

    @Test
    public void testCancelBadHttpResponseException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Cancel job, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.cancel();
    }

    @Test
    public void testPurgeJob() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        Assert.assertEquals("isPurged() should return the false", false, zosBatchJobSpy.isPurged());
        zosBatchJobSpy.purge();
        Assert.assertEquals("isPurged() should return the true", true, zosBatchJobSpy.isPurged());
        
        zosBatchJobSpy.purge();
    }

    @Test
    public void testPurgeZosmfException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");

        zosBatchJobSpy.purge();
    }

    @Test
    public void testPurgeResponseException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");

        zosBatchJobSpy.purge();
    }

    @Test
    public void testPurgeBadHttpResponseException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Purge job, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.purge();
    }

    @Test
    public void testUpdateJobStatus() throws ZosBatchException, ZosmfException  {
        String nullString = null;
        Whitebox.setInternalState(zosBatchJobSpy, "status", nullString);
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", nullString);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        JsonObject jsonObject = getJsonObject();
        jsonObject.remove("status");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());
        

        Whitebox.setInternalState(zosBatchJobSpy, "status", nullString);
        jsonObject.addProperty("status", "STATUS");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());

        
        Whitebox.setInternalState(zosBatchJobSpy, "status", nullString);
        jsonObject.addProperty("status", "OUTPUT");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertTrue("jobComplete should be true", zosBatchJobSpy.isComplete());
        

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
   public void testUpdateJobStatusZosmfException() throws ZosBatchException, ZosmfException {
       Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
       Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
       
       exceptionRule.expect(ZosBatchException.class);
       exceptionRule.expectMessage("exception");

       zosBatchJobSpy.updateJobStatus();
   }
   
  @Test
  public void testUpdateJobStatusZosmfResponseException() throws ZosBatchException, ZosmfException {
      Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
      Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
      
      Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException("exception"));
      
      exceptionRule.expect(ZosBatchException.class);
      exceptionRule.expectMessage("exception");

      zosBatchJobSpy.updateJobStatus();
  }
  
 @Test
 public void testUpdateJobStatusBadHttpResponseException() throws ZosBatchException, ZosmfException {
     Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
     Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

     Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
     Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
     
     exceptionRule.expect(ZosBatchException.class);
     exceptionRule.expectMessage(StringStartsWith.startsWith("Error Update job status, category:0, rc:0, reason:0, message:message"));

     zosBatchJobSpy.updateJobStatus();
 }
    
    @Test
    public void testAchiveJobOutput() throws ZosBatchException, ZosmfException {        
        ZosBatchJobOutputImpl zosBatchJobOutput = new ZosBatchJobOutputImpl(FIXED_JOBNAME, FIXED_JOBID);
        zosBatchJobOutput.addJcl("JCL");
        zosBatchJobOutput.add(getJsonObject(), "records");       
        PowerMockito.doReturn(zosBatchJobOutput).when(zosBatchJobSpy).jobOutput();
        
        Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
        Whitebox.setInternalState(zosBatchJobSpy, "jobname", zosJobnameMock);
        Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", FIXED_RETCODE_0000);        
        PowerMockito.doReturn(zosBatchJobOutput).when(zosBatchJobSpy).retrieveOutput();
        
        Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", false);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
        zosBatchJobSpy.archiveJobOutput();
        Assert.assertEquals("isArchived() should return the true", true, zosBatchJobSpy.isArchived());

        Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
        zosBatchJobSpy.archiveJobOutput();
        Assert.assertEquals("isArchived() should return the true", true, zosBatchJobSpy.isArchived());

        Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", false);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
        zosBatchJobSpy.archiveJobOutput();
        Assert.assertEquals("isArchived() should return the true", true, zosBatchJobSpy.isArchived());

        Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
        zosBatchJobSpy.archiveJobOutput();
        Assert.assertEquals("isArchived() should return the true", true, zosBatchJobSpy.isArchived());

        PowerMockito.doReturn(null).doReturn(zosBatchJobOutput).when(zosBatchJobSpy).jobOutput();
        Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", false);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
        zosBatchJobSpy.archiveJobOutput();
        Assert.assertEquals("isArchived() should return the true", true, zosBatchJobSpy.isArchived());
        
        PowerMockito.doReturn(null).when(zosBatchJobSpy).jobOutput();
    }
    
    @Test
    public void testAddOutputFileContent() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
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
    public void testAddOutputZosmfException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");

        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testAddOutputZosmfResponseException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        Mockito.when(zosmfResponseMockStatus.getTextContent()).thenThrow(new ZosmfException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testAddOutputZosmfResponseException1() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getTextContent()).thenReturn("content");
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND); 
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testAddOutputBadHttpResponseException() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
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
        Whitebox.setInternalState(zosBatchJobSpy, "jcl", "JCL\n"); 
        Assert.assertThat("jclWithJobcard() should return a valid job card with SYSAFF", zosBatchJobSpy.jclWithJobcard(), StringStartsWith.startsWith(jobWithJobcard));
        
    }
    
    @Test 
    public void testStoreArtifact() throws ZosBatchException, IOException {
        Path archivePathMock = newMockedPath(false);
        Answer<Path> resolveAnswer = new Answer<Path>() {
            @Override
            public Path answer(InvocationOnMock invocation) throws Throwable {
                String path = invocation.getArgument(0);
                if ("artifactPathElement2".equals(path) ||
                        ("artifact_0_Path_Element".equals(path) || 
                         "artifact_1_Path_Element".equals(path)) || 
                         "artifact_99_Path_Element".equals(path)) {
                    return newMockedPath(true);
                }
                return archivePathMock;
            }
        };
        Mockito.when(archivePathMock.resolve(Mockito.anyString())).thenAnswer(resolveAnswer);
        ZosBatchManagerImpl.setArchivePath(archivePathMock);
        
        zosBatchJobSpy.storeArtifact("content", "artifactPathElement");
        Assert.assertEquals("", 0, (int) Whitebox.getInternalState(zosBatchJobSpy, "uniqueId"));
        
        zosBatchJobSpy.storeArtifact("content", "artifactPathElement1", "artifactPathElement2");
        Assert.assertEquals("", 1, (int) Whitebox.getInternalState(zosBatchJobSpy, "uniqueId"));
        
        Whitebox.setInternalState(zosBatchJobSpy, "uniqueId", 0);
        zosBatchJobSpy.storeArtifact("content", "artifact_0_Path_Element");
        Assert.assertEquals("", 2, (int) Whitebox.getInternalState(zosBatchJobSpy, "uniqueId"));
        
        Whitebox.setInternalState(zosBatchJobSpy, "uniqueId", 99);
        zosBatchJobSpy.storeArtifact("content", "artifact_99_Path_Element");
        Assert.assertEquals("", 100, (int) Whitebox.getInternalState(zosBatchJobSpy, "uniqueId"));
    }
    
    @Test 
    public void testStoreArtifactNullPointerException() throws ZosBatchException {       
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Unable to get archive path");
        ZosBatchManagerImpl.setArchivePath(null);
        
        zosBatchJobSpy.storeArtifact("content", "artifactPathElements");
    }
    
    @Test 
    public void testStoreArtifactIOException() throws ZosBatchException, IOException {       
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Unable to store artifact");
        Path localArchivePathMock = newMockedPath(true);
        FileSystem localFileSystemMock = Mockito.mock(FileSystem.class);        
        FileSystemProvider localFileSystemProviderMock = Mockito.mock(FileSystemProvider.class);
        Mockito.when(localArchivePathMock.resolve(Mockito.anyString())).thenReturn(localArchivePathMock);
        Mockito.when(localArchivePathMock.getFileSystem()).thenReturn(localFileSystemMock);
        Mockito.when(localFileSystemMock.provider()).thenReturn(localFileSystemProviderMock);
        Mockito.when(localFileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IOException());
        Mockito.doThrow(new IOException()).when(localFileSystemProviderMock).checkAccess(Mockito.any(), Mockito.any());
        ZosBatchManagerImpl.setArchivePath(localArchivePathMock);
        
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
    
    @Test
    public void testTruncateJcl() throws ZosBatchManagerException {
        PowerMockito.mockStatic(TruncateJCLRecords.class);
        Mockito.when(TruncateJCLRecords.get(Mockito.any())).thenReturn(false);
        
        String suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
                             "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--";
        String returnedJcl = suppliedJcl;
        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
        
        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9";
        returnedJcl = suppliedJcl;
        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
        Assert.assertEquals("The value of intdrLrecl should be 90", 90, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
        
        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
        returnedJcl = suppliedJcl;
        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
        Assert.assertEquals("The value of intdrLrecl should be 90", 90, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
        Assert.assertEquals("the value of intdrRecfm should be V", "V", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
        
        PowerMockito.mockStatic(TruncateJCLRecords.class);
        Mockito.when(TruncateJCLRecords.get(Mockito.any())).thenReturn(true);
        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--";
        returnedJcl = suppliedJcl;
        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
    
        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9";
        returnedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8\n" +
                        "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
        Assert.assertEquals("JCL should be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));
        
        Whitebox.setInternalState(zosBatchJobSpy, "intdrLrecl", 80);
        Whitebox.setInternalState(zosBatchJobSpy, "intdrRecfm", "F");
        suppliedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
        returnedJcl = "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7--\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8\n" +
                      "----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8";
        Assert.assertEquals("JCL should not be truncated", returnedJcl, zosBatchJobSpy.parseJcl(suppliedJcl));
        Assert.assertEquals("The value of intdrLrecl should be 80", 80, (int) Whitebox.getInternalState(zosBatchJobSpy, "intdrLrecl"));
        Assert.assertEquals("the value of intdrRecfm should be F", "F", Whitebox.getInternalState(zosBatchJobSpy, "intdrRecfm"));

        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Unable to get trucate JCL records property value");
        Mockito.when(TruncateJCLRecords.get(Mockito.anyString())).thenThrow(new ZosBatchManagerException("exception"));
        
        zosBatchJobSpy.parseJcl(suppliedJcl);
    }
                 
    private Path newMockedPath(boolean fileExists) throws IOException {
        Path pathMock = Mockito.mock(Path.class);
        FileSystem fileSystemMock = Mockito.mock(FileSystem.class);
        FileSystemProvider fileSystemProviderMock = Mockito.mock(FileSystemProvider.class);
        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
        Mockito.when(pathMock.resolve(Mockito.anyString())).thenReturn(pathMock);        
        Mockito.when(pathMock.getFileSystem()).thenReturn(fileSystemMock);
        Mockito.when(fileSystemMock.provider()).thenReturn(fileSystemProviderMock);
        SeekableByteChannel seekableByteChannelMock = Mockito.mock(SeekableByteChannel.class);
        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(seekableByteChannelMock);
        Mockito.when(fileSystemProviderMock.newOutputStream(Mockito.any(Path.class), Mockito.any())).thenReturn(outputStreamMock);
        if (!fileExists) {
            Mockito.doThrow(new IOException()).when(fileSystemProviderMock).checkAccess(Mockito.any(), Mockito.any());
        }
        return pathMock;
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
