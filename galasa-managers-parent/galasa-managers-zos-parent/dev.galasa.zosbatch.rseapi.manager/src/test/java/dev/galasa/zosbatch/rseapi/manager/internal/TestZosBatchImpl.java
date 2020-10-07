/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.rseapi.manager.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

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
import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.internal.properties.InputClass;
import dev.galasa.zosbatch.internal.properties.MsgClass;
import dev.galasa.zosbatch.internal.properties.MsgLevel;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RseapiZosBatchManagerImpl.class, InputClass.class, MsgClass.class, MsgLevel.class})
public class TestZosBatchImpl {
    
    private RseapiZosBatchImpl zosBatch;
    
    private RseapiZosBatchImpl zosBatchSpy;

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private ZosManagerImpl zosManagerMock;

    @Mock
    private IZosBatchJobname zosJobnameMock;

    @Mock
    private ZosBatchJobcard zosBatchJobcardMock;

    @Mock
    private RseapiManagerImpl rseapiManagerMock;
    
    @Mock
    private List<IZosBatchJob> zosBatchJobListMock;
    
    @Mock
    private IRseapiRestApiProcessor rseapiApiProcessorMock;  
    
    @Mock
    private IRseapiResponse rseapiResponseMockSubmit;
    
    @Mock
    private IRseapiResponse rseapiResponseMockList;
    
    @Mock
    private IRseapiResponse rseapiResponseMockStatus;
    
    private static final String FIXED_JOBNAME = "GAL45678";
    
    private static final String FIXED_JOBID = "JOB12345";
    
    private static final String FIXED_OWNER = "USERID";
    
    private static final String FIXED_TYPE = "TYP";

    private static final String FIXED_STATUS_OUTPUT = "OUTPUT";

    private static final String FIXED_RETCODE_0000 = "CC 0000";
    
    @Mock
    private RseapiZosBatchJobImpl zosBatchJobMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setup() throws ZosBatchManagerException, IOException, NoSuchMethodException, SecurityException, RseapiManagerException {
        Path archivePathMock = Mockito.mock(Path.class);
        FileSystem fileSystemMock = Mockito.mock(FileSystem.class);
        FileSystemProvider fileSystemProviderMock = Mockito.mock(FileSystemProvider.class);
        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
        Mockito.when(archivePathMock.resolve(Mockito.anyString())).thenReturn(archivePathMock);
        Mockito.when(archivePathMock.getFileSystem()).thenReturn(fileSystemMock);
        Mockito.when(fileSystemMock.provider()).thenReturn(fileSystemProviderMock);
        SeekableByteChannel seekableByteChannelMock = Mockito.mock(SeekableByteChannel.class);
        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(seekableByteChannelMock);
        Mockito.when(fileSystemProviderMock.newOutputStream(Mockito.any(Path.class), Mockito.any())).thenReturn(outputStreamMock);
        Mockito.when(fileSystemMock.getPath(Mockito.anyString(), Mockito.any())).thenReturn(archivePathMock);
        Mockito.doThrow(new IOException()).when(fileSystemProviderMock).checkAccess(Mockito.any(), Mockito.any());
        RseapiZosBatchManagerImpl.setArchivePath(archivePathMock);
        RseapiZosBatchManagerImpl.setCurrentTestMethodArchiveFolderName(TestZosBatchImpl.class.getDeclaredMethod("setup").getName());        
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
        
        Mockito.when(zosManagerMock.newZosBatchJobname(Mockito.anyString())).thenReturn(zosJobnameMock);
        
        Mockito.when(zosManagerMock.newZosBatchJobname(Mockito.any(IZosImage.class))).thenReturn(zosJobnameMock);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyJobWaitTimeout(Mockito.any())).thenReturn(2);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyUseSysaff(Mockito.any())).thenReturn(false);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyBatchRestrictToImage(Mockito.any())).thenReturn(true);
        
        Mockito.when(zosManagerMock.getZosBatchPropertyTruncateJCLRecords(Mockito.any())).thenReturn(true);
        
        PowerMockito.mockStatic(InputClass.class);
        Mockito.when(InputClass.get(Mockito.any())).thenReturn("X");
        
        PowerMockito.mockStatic(MsgClass.class);
        Mockito.when(MsgClass.get(Mockito.any())).thenReturn("X");
        
        PowerMockito.mockStatic(MsgLevel.class);
        Mockito.when(MsgLevel.get(Mockito.any())).thenReturn("X");

        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        RseapiZosBatchManagerImpl.setRseapiManager(rseapiManagerMock);

        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockSubmit);
        Mockito.when(rseapiResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(rseapiResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
        
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMockStatus);
        Mockito.when(rseapiResponseMockStatus.getJsonArrayContent()).thenReturn(getJsonArray());
        Mockito.when(rseapiResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        RseapiZosBatchManagerImpl.setZosManager(zosManagerMock);
        
        zosBatch = new RseapiZosBatchImpl(zosImageMock);
        zosBatchSpy = Mockito.spy(zosBatch);
    }
    
    @Test
    public void testSubmitJob() throws Exception {
        IZosBatchJob zosBatchJob = zosBatchSpy.submitJob("JCL", null);
        Assert.assertEquals("getJobId() should return FIXED_JOBID", "????????", zosBatchJob.getJobId());
        
        zosBatchJob = zosBatchSpy.submitJob("JCL", zosJobnameMock, zosBatchJobcardMock);
        Assert.assertEquals("getJobname() should return mocked mocked ZosJobnameImpl", zosJobnameMock, zosBatchJob.getJobname());
        
        zosBatchJob = zosBatchSpy.submitJob("JCL", zosJobnameMock, null);
        Assert.assertEquals("getJobname() should return mocked mocked ZosJobnameImpl", zosJobnameMock, zosBatchJob.getJobname());

        Mockito.doThrow(new ZosBatchException("exception")).when(zosManagerMock).newZosBatchJobname(Mockito.any(IZosImage.class));      
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        zosBatchSpy.submitJob("JCL", null, null);
    }
    
    @Test
    public void testGetJobs() throws Exception {
        Mockito.doReturn(zosBatchJobListMock).when(zosBatchSpy).getBatchJobs(Mockito.any(), Mockito.any());
        Assert.assertEquals("getJobs() should return mocked List", zosBatchJobListMock, zosBatchSpy.getJobs(FIXED_JOBNAME, FIXED_OWNER));

        Assert.assertEquals("getJobs() should return mocked List", zosBatchJobListMock, zosBatchSpy.getJobs(null, null));
    }
    
    @Test
    public void testGetJobsException1() throws Exception {       
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Jobname must be between 1 and 8 characters or null");
        
        zosBatchSpy.getJobs("", null);
    }
    
    @Test
    public void testGetJobsException2() throws Exception {       
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Jobname must be between 1 and 8 characters or null");
        
        zosBatchSpy.getJobs("123456789", null);
    }
    
    @Test
    public void testGetJobsException3() throws Exception {       
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Owner must be between 1 and 8 characters or null");
        
        zosBatchSpy.getJobs(null, "");
    }
    
    @Test
    public void testGetJobsException4() throws Exception {       
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Owner must be between 1 and 8 characters or null");
        
        zosBatchSpy.getJobs(null, "123456789");
    }
    
    @Test
    public void testGetBatchJobs() throws Exception {
        List<IZosBatchJob> zosBatchJobs = zosBatchSpy.getBatchJobs(FIXED_JOBNAME, FIXED_JOBID);
        Assert.assertEquals("List returned by getBatchJobs() should contain FIXED_JOBID", 1, zosBatchJobs.size());
        
        Assert.assertEquals("List returned by getBatchJobs() should contain FIXED_JOBID", FIXED_JOBID, zosBatchJobs.get(0).getJobId());
        
        zosBatchJobs = zosBatchSpy.getBatchJobs(null, null);
        Assert.assertEquals("List returned by getBatchJobs() should contain FIXED_JOBID", 1, zosBatchJobs.size());
        
        Assert.assertEquals("List returned by getBatchJobs() should contain FIXED_JOBID", FIXED_JOBID, zosBatchJobs.get(0).getJobId());
    }
    
    @Test
    public void testGetBatchJobsException1() throws Exception {
    	Mockito.when(zosManagerMock.getZosBatchPropertyBatchRestrictToImage(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));      
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException2() throws Exception {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException3() throws Exception {
        Mockito.when(rseapiResponseMockStatus.getContent()).thenThrow(new RseapiException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException4() throws Exception {
        Mockito.when(rseapiResponseMockStatus.getJsonArrayContent()).thenThrow(new RseapiException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException5() throws Exception {
        Mockito.when(rseapiResponseMockStatus.getContent()).thenReturn(getJsonObject());
        Mockito.when(rseapiResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMockStatus.getStatusLine()).thenReturn("NOT_FOUND");
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("Error List jobs output, HTTP Status Code 404 : NOT_FOUND\nstatus: " + FIXED_STATUS_OUTPUT + "\nmessage: message");
                
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testCleanup() throws Exception {
        List<RseapiZosBatchJobImpl> zosBatchJobs = new ArrayList<>();
        Whitebox.setInternalState(zosBatchSpy, "zosBatchJobs", zosBatchJobs);
        zosBatchSpy.cleanup();
        Assert.assertEquals("zosBatchJobs should have 0 entries", new ArrayList<>(), Whitebox.getInternalState(zosBatchSpy, "zosBatchJobs"));

        zosBatchJobs = new ArrayList<>();
        Mockito.when(zosBatchJobMock.submitted()).thenReturn(false);
        zosBatchJobs.add(zosBatchJobMock);
        Whitebox.setInternalState(zosBatchSpy, "zosBatchJobs", zosBatchJobs);
        zosBatchSpy.cleanup();
        Assert.assertEquals("zosBatchJobs should have 0 entries", new ArrayList<>(), Whitebox.getInternalState(zosBatchSpy, "zosBatchJobs"));
        
        zosBatchJobs = new ArrayList<>();
        Mockito.when(zosBatchJobMock.submitted()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isComplete()).thenReturn(false);
        Mockito.when(zosBatchJobMock.isArchived()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isPurged()).thenReturn(true);
        zosBatchJobs.add(zosBatchJobMock);
        Whitebox.setInternalState(zosBatchSpy, "zosBatchJobs", zosBatchJobs);
        zosBatchSpy.cleanup();
        Assert.assertEquals("zosBatchJobs should have 0 entries", new ArrayList<>(), Whitebox.getInternalState(zosBatchSpy, "zosBatchJobs"));

        zosBatchJobs = new ArrayList<>();
        Mockito.when(zosBatchJobMock.submitted()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isComplete()).thenReturn(false);
        Mockito.when(zosBatchJobMock.isArchived()).thenReturn(false);
        Mockito.when(zosBatchJobMock.isPurged()).thenReturn(false);
        zosBatchJobs.add(zosBatchJobMock);
        Whitebox.setInternalState(zosBatchSpy, "zosBatchJobs", zosBatchJobs);
        zosBatchSpy.cleanup();
        Assert.assertEquals("zosBatchJobs should have 0 entries", new ArrayList<>(), Whitebox.getInternalState(zosBatchSpy, "zosBatchJobs"));

        zosBatchJobs = new ArrayList<>();
        Mockito.when(zosBatchJobMock.submitted()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isComplete()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isArchived()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isPurged()).thenReturn(true);
        zosBatchJobs.add(zosBatchJobMock);
        Whitebox.setInternalState(zosBatchSpy, "zosBatchJobs", zosBatchJobs);
        zosBatchSpy.cleanup();
        Assert.assertEquals("zosBatchJobs should have 0 entries", new ArrayList<>(), Whitebox.getInternalState(zosBatchSpy, "zosBatchJobs"));

        zosBatchJobs = new ArrayList<>();
        Mockito.when(zosBatchJobMock.submitted()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isComplete()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isArchived()).thenReturn(false);
        Mockito.when(zosBatchJobMock.isPurged()).thenReturn(false);
        zosBatchJobs.add(zosBatchJobMock);
        Whitebox.setInternalState(zosBatchSpy, "zosBatchJobs", zosBatchJobs);
        zosBatchSpy.cleanup();
        Assert.assertEquals("zosBatchJobs should have 0 entries", new ArrayList<>(), Whitebox.getInternalState(zosBatchSpy, "zosBatchJobs"));

        zosBatchJobs = new ArrayList<>();
        Mockito.when(zosBatchJobMock.submitted()).thenReturn(true);
        Mockito.when(zosBatchJobMock.isComplete()).thenReturn(false);
        Mockito.doThrow(new ZosBatchException()).when(zosBatchJobMock).cancel();
        zosBatchJobs.add(zosBatchJobMock);
        Whitebox.setInternalState(zosBatchSpy, "zosBatchJobs", zosBatchJobs);
        zosBatchSpy.cleanup();
        Assert.assertEquals("zosBatchJobs should have 0 entries", new ArrayList<>(), Whitebox.getInternalState(zosBatchSpy, "zosBatchJobs"));
    }
   
    private JsonObject getJsonObject() {
        JsonObject responseBody = new JsonObject();
        responseBody.addProperty("jobName", FIXED_JOBNAME);
        responseBody.addProperty("jobID", FIXED_JOBID);
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
