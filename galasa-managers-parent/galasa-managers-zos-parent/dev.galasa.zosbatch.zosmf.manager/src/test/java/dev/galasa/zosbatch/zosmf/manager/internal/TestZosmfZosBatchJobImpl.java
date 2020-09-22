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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.spi.IZosBatchJobOutputSpi;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosmfZosBatchManagerImpl.class, LogFactory.class})
public class TestZosmfZosBatchJobImpl {
    
    private ZosmfZosBatchJobImpl zosBatchJob;
    
    private ZosmfZosBatchJobImpl zosBatchJobSpy;
    
    @Mock
    private Log logMock;
    
    private static String logMessage;

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private IZosBatchJobname zosJobnameMock;

    @Mock
    private ZosBatchJobcard zosBatchJobcardMock;

    @Mock
    private ZosManagerImpl zosManagerMock;

    @Mock
    private ZosmfManagerImpl zosmfManagerMock;
    
    @Mock
    private IZosmfRestApiProcessor zosmfApiProcessorMock;
    
    @Mock
    private IZosmfResponse zosmfResponseMockSubmit;
    
    @Mock
    private IZosmfResponse zosmfResponseMockStatus;
    
    @Mock
    private IZosBatchJobOutputSpi zosBatchJobOutputMock;
    
    @Mock
    private IZosBatchJobOutputSpoolFile zosBatchJobOutputSpoolFileMock;
    
    @Mock
    private Iterator<IZosBatchJobOutputSpoolFile> zosBatchJobOutputSpoolFileIteratorMock;
    
    @Mock
    private ResultArchiveStorePath resultArchiveStorePathMock;

    private static final String FIXED_IMAGE_ID = "IMAGE";

    private static final String FIXED_JOBNAME = "GAL45678";
    
    private static final String FIXED_JOBID = "JOB12345";
    
    private static final String FIXED_OWNER = "USERID";
    
    private static final String FIXED_JOBCARD = "//" + FIXED_JOBNAME + " JOB \n";
    
    private static final String FIXED_TYPE = "TYP";
    
    private static final String FIXED_STATUS_OUTPUT = "OUTPUT";

    private static final String FIXED_RETCODE_0000 = "CC 0000";

    private static final String FIXED_RETCODE_0020 = "CC 0020";

	private static final String FIXED_DDNAME = "DDNAME";

	private static final Object FIXED_STEPNAME = "STEP";

	private static final Object FIXED_PROCSTEP = "PROCSTEP";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
        Answer<String> answer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                logMessage = invocation.getArgument(0);
                System.err.println("Captured Log Message:\n" + logMessage);
                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
                    ((Throwable) invocation.getArgument(1)).printStackTrace();
                }
                return null;
            }
        };
        Mockito.doAnswer(answer).when(logMock).info(Mockito.any());
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(FIXED_IMAGE_ID);
        
        Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyJobWaitTimeout(Mockito.any())).thenReturn(2);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyUseSysaff(Mockito.any())).thenReturn(false);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyRestrictToImage(Mockito.any())).thenReturn(true);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.any())).thenReturn(true);        
        
        ZosmfZosBatchManagerImpl.setArchivePath(newMockedPath(false));
        ZosmfZosBatchManagerImpl.setCurrentTestMethodArchiveFolderName(TestZosmfZosBatchJobImpl.class.getDeclaredMethod("setup").getName());

        PowerMockito.doReturn(zosmfApiProcessorMock).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        ZosmfZosBatchManagerImpl.setZosmfManager(zosmfManagerMock);
        ZosmfZosBatchManagerImpl.setZosManager(zosManagerMock);
        
        Mockito.when(zosBatchJobcardMock.getJobcard(Mockito.any(), Mockito.any())).thenReturn(FIXED_JOBCARD);
        
        zosBatchJob = new ZosmfZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL", zosBatchJobcardMock);
        zosBatchJobSpy = Mockito.spy(zosBatchJob);
    }
    
    @Test
    public void testConstructor() throws ZosBatchException {
        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
        
        zosBatchJob = new ZosmfZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL", null);
        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
        
        zosBatchJob = new ZosmfZosBatchJobImpl(zosImageMock, zosJobnameMock, null, null);
        Assert.assertEquals("getJobname() should return the supplied job name", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
    }
    
    @Test
    public void testConstructorJobWaitTimeoutException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Unable to get job timeout property value");
        Mockito.when(zosManagerMock.getZosBatchPropertyJobWaitTimeout(Mockito.anyString())).thenThrow(new ZosBatchManagerException("exception"));
        
        new ZosmfZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL", null);
    }
    
    @Test
    public void testConstructorUseSysaffException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Unable to get use SYSAFF property value");
        Mockito.when(zosManagerMock.getZosBatchPropertyUseSysaff(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));
        
        new ZosmfZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL", null);
    }
    
    @Test
    public void testConstructorRestrictToImageException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosManagerMock.getZosBatchPropertyRestrictToImage(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));
        
        new ZosmfZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL", null);
    }
    
    @Test
    public void testConstructorStoreArtifactException() throws ZosManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("exception");
        PowerMockito.doThrow(new ZosManagerException("exception")).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
        
        new ZosmfZosBatchJobImpl(zosImageMock, zosJobnameMock, "JCL", null);
    }
    
    @Test
    public void testGetJobId() {
        Assert.assertEquals("getJobId() should return the 'unknown' value", "????????", zosBatchJobSpy.getJobId());
        zosBatchJobSpy.setJobid(FIXED_JOBID);
        Assert.assertEquals("getJobId() should return the supplied value", FIXED_JOBID, zosBatchJobSpy.getJobId());
    }
    
    @Test
    public void testGetOwner() {
        Assert.assertEquals("getOwner() should return the 'unknown' value", "????????", zosBatchJobSpy.getOwner());
        zosBatchJobSpy.setOwner(FIXED_OWNER);
        Assert.assertEquals("getOwner() should return the supplied value", FIXED_OWNER, zosBatchJobSpy.getOwner());
    }
    
    @Test
    public void testGetType() {
        Assert.assertEquals("getType() should return the 'unknown' value", "???", zosBatchJobSpy.getType());
        zosBatchJobSpy.setType(FIXED_TYPE);
        Assert.assertEquals("getType() should return the supplied value", FIXED_TYPE, zosBatchJobSpy.getType());
    }
    
    @Test
    public void testGetStatus() {
        Assert.assertEquals("getStatus() should return the 'unknown' value", "????????", zosBatchJobSpy.getStatus());
        zosBatchJobSpy.setStatus(FIXED_STATUS_OUTPUT);
        Assert.assertEquals("getStatus() should return the 'unknown' value", FIXED_STATUS_OUTPUT, zosBatchJobSpy.getStatus());
    }
    
    @Test
    public void testGetRetcode() {
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
        Assert.assertEquals("getRetcode() should return the 'unknown' value", "????", zosBatchJobSpy.getRetcode());
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", FIXED_RETCODE_0000);
        Assert.assertEquals("getRetcode() should return the supplied value", FIXED_RETCODE_0000, zosBatchJobSpy.getRetcode());
    }
    
    @Test
    public void testSubmitJob() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosBatchJobSpy.submitJob();
        Assert.assertEquals("getJobname().getName() should return the supplied value", FIXED_JOBNAME, zosBatchJobSpy.getJobname().getName());
    }
    
    @Test
    public void testSubmitJobIZosmfRestApiProcessorSendRequestException() throws ZosBatchException, ZosmfException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        zosBatchJobSpy.submitJob();
    }
    
    @Test
    public void testSubmitIZosmfResponseGetJsonContentException() throws ZosBatchException, ZosmfException {        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        zosBatchJobSpy.submitJob();
    }
    
    @Test
    public void testSubmitJobNotStatusCodeCreated() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Submit job, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.submitJob();
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

        Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", true);
        Mockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Assert.assertEquals("waitForJob() should return the Integer.MIN_VALUE", Integer.MIN_VALUE, zosBatchJobSpy.waitForJob());
    }

    @Test
    public void testWaitForJobNotSubmittedException() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Job has not been submitted by manager");
        zosBatchJobSpy.waitForJob();
    }
    
    @Test
    public void testRetrieveOutput() throws Exception {        
    	Mockito.doNothing().when(zosBatchJobSpy).getOutput();
    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).jobOutput();
    	Whitebox.setInternalState(zosBatchJobSpy, "outputComplete", false);
        Assert.assertEquals("retrieveOutput() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.retrieveOutput());
        
        Whitebox.setInternalState(zosBatchJobSpy, "outputComplete", true);
        Assert.assertEquals("retrieveOutput() should return expected value", zosBatchJobOutputMock, zosBatchJobSpy.retrieveOutput());
    }
    
    @Test
    public void testRetrieveOutputNotSubmittedException() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Job has not been submitted by manager");
        zosBatchJobSpy.retrieveOutput();
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
    public void testRetrieveOutputAsString() throws Exception {
    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).retrieveOutput();
        Mockito.doReturn("RECORDS\n").when(zosBatchJobOutputSpoolFileMock).getRecords();
    	List<IZosBatchJobOutputSpoolFile> spoolFiles = new ArrayList<IZosBatchJobOutputSpoolFile>();
    	spoolFiles.add(zosBatchJobOutputSpoolFileMock);
    	spoolFiles.add(zosBatchJobOutputSpoolFileMock);
		Mockito.doReturn(spoolFiles).when(zosBatchJobOutputMock).getSpoolFiles();
		String expected = "RECORDS\nRECORDS\n";
		Assert.assertEquals("retrieveOutputAsString() should return the expected value", expected, zosBatchJobSpy.retrieveOutputAsString());
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
    public void testPurge() throws ZosBatchException, ZosmfException {
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
    public void testGetSpoolFile() throws ZosBatchException, ZosmfException {
    	Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
        Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).retrieveOutput();
        Mockito.doReturn(zosBatchJobOutputSpoolFileIteratorMock).when(zosBatchJobOutputMock).iterator();
        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
        Mockito.doReturn(FIXED_DDNAME).when(zosBatchJobOutputSpoolFileMock).getDdname();
        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosBatchJobOutputSpoolFileIteratorMock).next();
		Assert.assertEquals("getSpoolFile() should return the mocked IZosBatchJobOutputSpoolFile", zosBatchJobOutputSpoolFileMock, zosBatchJobSpy.getSpoolFile(FIXED_DDNAME));

        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
		Assert.assertNull("getSpoolFile() should return the mocked IZosBatchJobOutputSpoolFile", zosBatchJobSpy.getSpoolFile("DUMMY"));
    }
    
    @Test
    public void testSaveOutputToTestResultsArchive() throws ZosManagerException {
        ZosmfZosBatchManagerImpl.setZosManager(zosManagerMock);
        PowerMockito.doNothing().when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
    	Whitebox.setInternalState(zosBatchJobSpy, "jobid", FIXED_JOBID);
    	Whitebox.setInternalState(zosBatchJobSpy, "retcode", FIXED_RETCODE_0000);
    	Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutputMock);
        Mockito.doReturn(zosBatchJobOutputSpoolFileIteratorMock).when(zosBatchJobOutputMock).iterator();
        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
        Mockito.doReturn(zosBatchJobOutputSpoolFileMock).when(zosBatchJobOutputSpoolFileIteratorMock).next();
        Mockito.doReturn(FIXED_JOBNAME).when(zosBatchJobOutputSpoolFileMock).getJobname();
        Mockito.doReturn(FIXED_JOBID).when(zosBatchJobOutputSpoolFileMock).getJobid();
        Mockito.doReturn("").when(zosBatchJobOutputSpoolFileMock).getStepname();
        Mockito.doReturn("").when(zosBatchJobOutputSpoolFileMock).getProcstep();
        Mockito.doReturn(FIXED_DDNAME).when(zosBatchJobOutputSpoolFileMock).getDdname();
        Mockito.doReturn("content").when(zosBatchJobOutputSpoolFileMock).getRecords();
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);

        String expectedMessage = "        " + FIXED_JOBNAME + "_" + FIXED_JOBID + "_" + FIXED_DDNAME;
    	zosBatchJobSpy.saveOutputToTestResultsArchive();
        Assert.assertEquals("saveOutputToTestResultsArchive() should log expected message", expectedMessage, logMessage);

        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
    	Mockito.doReturn(null, zosBatchJobOutputMock).when(zosBatchJobSpy).jobOutput();
    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).retrieveOutput();
        Mockito.doReturn(FIXED_STEPNAME).when(zosBatchJobOutputSpoolFileMock).getStepname();
        Mockito.doReturn(FIXED_PROCSTEP).when(zosBatchJobOutputSpoolFileMock).getProcstep();
        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
		
        expectedMessage = "        " + FIXED_JOBNAME + "_" + FIXED_JOBID + "_" + FIXED_STEPNAME + "_" + FIXED_PROCSTEP + "_" + FIXED_DDNAME;
    	zosBatchJobSpy.saveOutputToTestResultsArchive();
        Assert.assertEquals("saveOutputToTestResultsArchive() should log expected message", expectedMessage, logMessage);

    	Mockito.doReturn(zosBatchJobOutputMock).when(zosBatchJobSpy).jobOutput();
        Mockito.doReturn(true, false).when(zosBatchJobOutputSpoolFileIteratorMock).hasNext();
        PowerMockito.doThrow(new ZosManagerException("exception")).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
    	zosBatchJobSpy.saveOutputToTestResultsArchive();
    }
    
    @Test
    public void testGetOutput() throws ZosBatchException, ZosmfException {
    	PowerMockito.doReturn(true).when(zosBatchJobSpy).submitted();
        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        
        Mockito.when(zosmfResponseMockStatus.getJsonArrayContent()).thenReturn(getJsonArray());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        PowerMockito.doNothing().when(zosBatchJobSpy).addOutputFileContent(Mockito.any(), Mockito.any());
    	zosBatchJobSpy.getOutput();
    	
    	Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", true);
    	zosBatchJobSpy.getOutput();
    	
    	Whitebox.setInternalState(zosBatchJobSpy, "jobNotFound", false);
    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
    	zosBatchJobSpy.getOutput();
    	Assert.assertTrue("getOutput() should set outputComplete to true", Whitebox.getInternalState(zosBatchJobSpy, "outputComplete"));
    }

    @Test
    public void testUpdateJobStatus() throws ZosBatchException, ZosmfException  {
        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        JsonObject jsonObject = getJsonObject();
        jsonObject.remove("status");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());
        

        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
        jsonObject.addProperty("status", "STATUS");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertFalse("jobComplete should be false", zosBatchJobSpy.isComplete());

        
        Whitebox.setInternalState(zosBatchJobSpy, "status", (String) null);
        jsonObject.addProperty("status", "OUTPUT");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertTrue("jobComplete should be true", zosBatchJobSpy.isComplete());
        

        Whitebox.setInternalState(zosBatchJobSpy, "retcode", (String) null);
        jsonObject.remove("retcode");
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        
        zosBatchJobSpy.updateJobStatus();
        Assert.assertEquals("retcode should be ????", "????", zosBatchJobSpy.getRetcode());

        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        jsonObject.addProperty("rc", 4);
        jsonObject.addProperty("reason", 10);
        zosBatchJobSpy.updateJobStatus();
        Assert.assertTrue("jobNotFound should be true", Whitebox.getInternalState(zosBatchJobSpy, "jobNotFound"));

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
    public void testUpdateJobStatusBadHttpResponseException1() throws ZosBatchException, ZosmfException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Update job status, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.updateJobStatus();
    }

    @Test
    public void testUpdateJobStatusBadHttpResponseException2() throws ZosBatchException, ZosmfException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        JsonObject jsonObject = getJsonObject();
        jsonObject.addProperty("rc", 4);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Update job status, category:0, rc:4, reason:0, message:message"));

        zosBatchJobSpy.updateJobStatus();
    }

    @Test
    public void testUpdateJobStatusBadHttpResponseException3() throws ZosBatchException, ZosmfException {
        Mockito.doReturn(true).when(zosBatchJobSpy).submitted();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);

        JsonObject jsonObject = getJsonObject();
        jsonObject.addProperty("reason", 10);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Update job status, category:0, rc:0, reason:10, message:message"));

        zosBatchJobSpy.updateJobStatus();
    }
    
    @Test
    public void testAddOutputFileContent() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Whitebox.setInternalState(zosBatchJobSpy, "jobOutput", zosBatchJobOutputMock);

        zosBatchJobSpy.addOutputFileContent(null, null);
        
        JsonObject jsonObject = getJsonObject();
        zosBatchJobSpy.addOutputFileContent(jsonObject, null);

        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
        PowerMockito.doReturn(true).when(zosBatchJobSpy).spoolFileNotFound(Mockito.any());
        zosBatchJobSpy.addOutputFileContent(jsonObject, null);
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");

        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testAddOutputFileContentZosmfResponseException1() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        Mockito.when(zosmfResponseMockStatus.getTextContent()).thenThrow(new ZosmfException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testAddOutputFileContentZosmfResponseException2() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getTextContent()).thenReturn("content");
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND); 
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testAddOutputBadHttpResponseException1() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Retrieve job output, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testAddOutputBadHttpResponseException2() throws ZosBatchException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
        
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error Retrieve job output, category:0, rc:0, reason:0, message:message"));

        zosBatchJobSpy.addOutputFileContent(null, null);
    }
    
    @Test
    public void testJclWithJobcard() throws ZosBatchManagerException {
        Whitebox.setInternalState(zosBatchJobSpy, "useSysaff", false);
        String jobcard = "//" + FIXED_JOBNAME + " JOB @@@@,\n" + 
                         "//         CLASS=A,\n" + 
                         "//         MSGCLASS=A,\n" + 
                         "//         MSGLEVEL=(1,1)";
        jobcard.replace("@@@@", "");
        
        
        String expectedJobcard = "//" + FIXED_JOBNAME + " JOB \nJCL\n";
        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobSpy.jclWithJobcard());
        
        Whitebox.setInternalState(zosBatchJobSpy, "useSysaff", true);
        expectedJobcard = "//" + FIXED_JOBNAME + " JOB \n/*JOBPARM SYSAFF=" + FIXED_IMAGE_ID + "\nJCL\n";
        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobSpy.jclWithJobcard());

        Whitebox.setInternalState(zosBatchJobSpy, "jcl", "JCL\n");
        Assert.assertEquals("jclWithJobcard() should a return valid job card", expectedJobcard, zosBatchJobSpy.jclWithJobcard());
    }
    
    @Test
    public void testBuildErrorString() {
        String expectedString = "Error action";
        String returnString = ZosmfZosBatchJobImpl.buildErrorString("action", new JsonObject());
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        JsonObject jsonObject = getJsonObject();
        jsonObject.addProperty("details", "details");
        expectedString = "Error action, category:0, rc:0, reason:0, message:message\n" + 
                "details:details\n" + 
                "stack:\n" + 
                "stack";
        returnString = ZosmfZosBatchJobImpl.buildErrorString("action", jsonObject);
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
        returnString = ZosmfZosBatchJobImpl.buildErrorString("action", jsonObject);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);

        jsonObject.remove("stack");
        expectedString = "Error action, category:0, rc:0, reason:0, message:message\n" + 
                "details:\n" +
                "details line 1\n" +
                "details line 2";
        returnString = ZosmfZosBatchJobImpl.buildErrorString("action", jsonObject);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
    }
    
    @Test
    public void testArchiveJobOutput() throws Exception {
    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", false);
    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
    	Mockito.doNothing().when(zosBatchJobSpy).saveOutputToTestResultsArchive();
    	zosBatchJobSpy.archiveJobOutput();
    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(1)).invoke("saveOutputToTestResultsArchive");

    	Mockito.clearInvocations(zosBatchJobSpy);
    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", true);
    	zosBatchJobSpy.archiveJobOutput();
    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(0)).invoke("saveOutputToTestResultsArchive");

    	Mockito.clearInvocations(zosBatchJobSpy);
    	Whitebox.setInternalState(zosBatchJobSpy, "jobArchived", true);
    	Whitebox.setInternalState(zosBatchJobSpy, "jobComplete", false);
    	zosBatchJobSpy.archiveJobOutput();
    	PowerMockito.verifyPrivate(zosBatchJobSpy, Mockito.times(1)).invoke("saveOutputToTestResultsArchive");
    }
    
    @Test
    public void testJosnNull() {
        JsonObject JsonObject = new JsonObject();
        Assert.assertNull("jsonNull() should return null", zosBatchJobSpy.jsonNull(JsonObject, "none"));

        JsonObject.add("empty", JsonNull.INSTANCE);
        Assert.assertNull("jsonNull() should return null", zosBatchJobSpy.jsonNull(JsonObject, "empty"));

        JsonObject.addProperty("property", "value");
        Assert.assertEquals("jsonNull() should return value", "value", zosBatchJobSpy.jsonNull(JsonObject, "property"));
    }
    
    @Test
    public void testSpoolFileNotFound() {
        JsonObject JsonObject = new JsonObject();
        Assert.assertFalse("spoolFileNotFound() should return false", zosBatchJobSpy.spoolFileNotFound(JsonObject));

        JsonObject.addProperty("category", 6);
        Assert.assertFalse("spoolFileNotFound() should return false", zosBatchJobSpy.spoolFileNotFound(JsonObject));

        JsonObject.addProperty("rc", 4);
        Assert.assertFalse("spoolFileNotFound() should return false", zosBatchJobSpy.spoolFileNotFound(JsonObject));

        JsonObject.addProperty("reason", 12);
        Assert.assertTrue("spoolFileNotFound() should return true", zosBatchJobSpy.spoolFileNotFound(JsonObject));
    }
    
    @Test
    public void testJosnZero() {
        JsonObject JsonObject = new JsonObject();
        Assert.assertEquals("jsonZero() should return 0", 0, zosBatchJobSpy.jsonZero(JsonObject, "none"));

        JsonObject.add("empty", JsonNull.INSTANCE);
        Assert.assertEquals("jsonZero() should return 0", 0, zosBatchJobSpy.jsonZero(JsonObject, "empty"));

        JsonObject.addProperty("property", 99);
        Assert.assertEquals("jsonZero() should return 99", 99, zosBatchJobSpy.jsonZero(JsonObject, "property"));
    }
    
    @Test
    public void testToString() throws ZosBatchException {
        PowerMockito.doNothing().when(zosBatchJobSpy).updateJobStatus();        
        PowerMockito.doReturn(true).when(zosBatchJobSpy).isPurged();
        Whitebox.setInternalState(zosBatchJobSpy, "jobid", "#JOBID#");
        Whitebox.setInternalState(zosBatchJobSpy, "status", "#STATUS#");
        Whitebox.setInternalState(zosBatchJobSpy, "owner", "#OWNER#");
        Whitebox.setInternalState(zosBatchJobSpy, "type", "#TYPE#");
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", "#RETCODE#");
        Whitebox.setInternalState(zosBatchJobSpy, "retcode", "#RETCODE#");
        String expectedString = "JOBID=#JOBID# JOBNAME=" + FIXED_JOBNAME + " OWNER=#OWNER# TYPE=#TYPE# STATUS=#STATUS# RETCODE=#RETCODE#";
        Assert.assertEquals("toString() should return supplied value", expectedString, zosBatchJobSpy.toString());
        
        PowerMockito.doReturn(false).when(zosBatchJobSpy).isPurged();
        Assert.assertEquals("toString() should return supplied value", expectedString, zosBatchJobSpy.toString());
        
        PowerMockito.doThrow(new ZosBatchException("exception")).when(zosBatchJobSpy).updateJobStatus();
        zosBatchJobSpy.toString();
    }
    
    @Test
    public void testTruncateJcl() throws ZosBatchManagerException {
        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.any())).thenReturn(false);
        
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
        
        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.any())).thenReturn(true);
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
        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.anyString())).thenThrow(new ZosBatchManagerException("exception"));
        
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
        responseBody.addProperty("owner", FIXED_OWNER);
        responseBody.addProperty("type", FIXED_TYPE);
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
