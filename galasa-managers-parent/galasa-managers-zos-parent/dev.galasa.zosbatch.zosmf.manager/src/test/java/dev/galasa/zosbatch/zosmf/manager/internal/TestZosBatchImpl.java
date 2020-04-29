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

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.JobnamePrefix;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.RestrictToImage;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.TruncateJCLRecords;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.UseSysaff;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.ZosBatchZosmfPropertiesSingleton;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosBatchManagerImpl.class, JobWaitTimeout.class, UseSysaff.class, RestrictToImage.class, TruncateJCLRecords.class, JobnamePrefix.class})
public class TestZosBatchImpl {
    
    private ZosBatchImpl zosBatch;
    
    private ZosBatchImpl zosBatchSpy;

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private ZosBatchJobnameImpl zosJobnameMock;

    @Mock
    private ZosBatchJobcard zosBatchJobcardMock;

    @Mock
    private ZosmfManagerImpl zosmfManagerMock;
    
    @Mock
    private List<IZosBatchJob> zosBatchJobListMock;
    
    @Mock
    private IZosmfRestApiProcessor zosmfApiProcessorMock;  
    
    @Mock
    private IZosmfResponse zosmfResponseMockSubmit;
    
    @Mock
    private IZosmfResponse zosmfResponseMockList;
    
    @Mock
    private IZosmfResponse zosmfResponseMockStatus;
    
    private static final String FIXED_JOBNAME = "GAL45678";
    
    private static final String FIXED_JOBID = "JOB12345";
    
    private static final String FIXED_OWNER = "USERID";
    
    private static final String FIXED_TYPE = "TYP";

    private static final String FIXED_STATUS_OUTPUT = "OUTPUT";

    private static final String FIXED_RETCODE_0000 = "CC 0000";
    
    @Mock
    private ZosBatchJobImpl zosBatchJobMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setup() throws ZosBatchManagerException, IOException, NoSuchMethodException, SecurityException, ZosmfManagerException {
        
        IConfigurationPropertyStoreService cps = Mockito.mock(IConfigurationPropertyStoreService.class);
        ZosBatchZosmfPropertiesSingleton singleton = new ZosBatchZosmfPropertiesSingleton();
        singleton.activate();
        ZosBatchZosmfPropertiesSingleton.setCps(cps);        
        
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
        ZosBatchManagerImpl.setArchivePath(archivePathMock);
        ZosBatchManagerImpl.setCurrentTestMethod(TestZosBatchImpl.class.getDeclaredMethod("setup"));        
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        Mockito.when(zosJobnameMock.getName()).thenReturn(FIXED_JOBNAME);
        
        PowerMockito.mockStatic(JobnamePrefix.class);
        Mockito.when(JobnamePrefix.get(Mockito.anyString())).thenReturn(FIXED_JOBNAME);
        
        PowerMockito.mockStatic(JobWaitTimeout.class);
        Mockito.when(JobWaitTimeout.get(Mockito.any())).thenReturn(2);
        
        PowerMockito.mockStatic(UseSysaff.class);
        Mockito.when(UseSysaff.get(Mockito.any())).thenReturn(false);
        
        PowerMockito.mockStatic(RestrictToImage.class);
        Mockito.when(RestrictToImage.get(Mockito.any())).thenReturn(true);
        
        PowerMockito.mockStatic(TruncateJCLRecords.class);
        Mockito.when(TruncateJCLRecords.get(Mockito.any())).thenReturn(true);

        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictToImage.get(zosImageMock.getImageID()))).thenReturn(zosmfApiProcessorMock);
        ZosBatchManagerImpl.setZosmfManager(zosmfManagerMock);

        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockSubmit);
        Mockito.when(zosmfResponseMockSubmit.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockSubmit.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMockStatus);
        Mockito.when(zosmfResponseMockStatus.getJsonArrayContent()).thenReturn(getJsonArray());
        Mockito.when(zosmfResponseMockStatus.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosBatch = new ZosBatchImpl(zosImageMock);
        zosBatchSpy = Mockito.spy(zosBatch);
    }
    
    @Test
    public void testSubmitJob() throws Exception {
        IZosBatchJob zosBatchJob = zosBatchSpy.submitJob("JCL", null);
        Assert.assertEquals("getJobId() should return FIXED_JOBID", FIXED_JOBID, zosBatchJob.getJobId());
        
        zosBatchJob = zosBatchSpy.submitJob("JCL", zosJobnameMock, zosBatchJobcardMock);
        Assert.assertEquals("getJobname() should return mocked mocked ZosJobnameImpl", zosJobnameMock, zosBatchJob.getJobname());
        
        zosBatchJob = zosBatchSpy.submitJob("JCL", zosJobnameMock, null);
        Assert.assertEquals("getJobname() should return mocked mocked ZosJobnameImpl", zosJobnameMock, zosBatchJob.getJobname());
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
        Mockito.when(RestrictToImage.get(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));      
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException2() throws Exception {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException3() throws Exception {
        Mockito.when(zosmfResponseMockStatus.getContent()).thenThrow(new ZosmfException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException4() throws Exception {
        Mockito.when(zosmfResponseMockStatus.getJsonArrayContent()).thenThrow(new ZosmfException("exception"));
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage("exception");
        
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testGetBatchJobsException5() throws Exception {
        Mockito.when(zosmfResponseMockStatus.getContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMockStatus.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Error List jobs output, category:0, rc:0, reason:0, message:message"));
                
        zosBatchSpy.getJobs(null, null);
    }
    
    @Test
    public void testCleanup() throws Exception {
        List<ZosBatchJobImpl> zosBatchJobs = new ArrayList<>();
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
