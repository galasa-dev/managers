/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
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
import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class})
public class TestRseapiZosDatasetImpl {
    
    private RseapiZosDatasetImpl zosDataset;
    
    private RseapiZosDatasetImpl zosDatasetSpy;

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private ZosManagerImpl zosManagerMock;
    
    @Mock
    private RseapiManagerImpl rseapiManagerMock;
    
    @Mock
    private IRseapiRestApiProcessor rseapiApiProcessorMock;
    
    @Mock
    private IRseapiResponse rseapiResponseMock;
    
    @Mock
    private RseapiZosDatasetAttributesListdsi zosDatasetAttributesListdsiMock;
    
    @Mock
    private Log logMock;
    
    private static String logMessage;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String DATASET_NAME = "DATA.SET.NAME";
    
    private static final String MEMBER_NAME = "MEMBER";
    
    private static final String IMAGE = "IMAGE";
    
    private static final String CONTENT = "content";
    
    private static final String EXCEPTION = "exception";

	private static final String PATH_MOCK = "PATH_MOCK";
    
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
        Mockito.doAnswer(answer).when(logMock).warn(Mockito.any());
        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        
        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
        RseapiZosFileManagerImpl.setZosManager(zosManagerMock);

        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        RseapiZosFileManagerImpl.setRseapiManager(rseapiManagerMock);
        
        zosDataset = new RseapiZosDatasetImpl(zosImageMock, DATASET_NAME);
        zosDatasetSpy = Mockito.spy(zosDataset);
    }
    
    @Test
    public void testConstructorException() throws RseapiManagerException, ZosFileManagerException {
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        Mockito.doThrow(new RseapiManagerException(EXCEPTION)).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        new RseapiZosDatasetImpl(zosImageMock, DATASET_NAME);
    }
    
    @Test
    public void testCreate() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();

        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
       
        zosDatasetSpy.create();        
        Assert.assertFalse("created() should return false", zosDatasetSpy.created());

        Whitebox.setInternalState(zosDatasetSpy, "alcunit", SpaceUnit.TRACKS);
        Whitebox.setInternalState(zosDatasetSpy, "dstype", DSType.PDSE);
        zosDatasetSpy.create();
        
        Whitebox.setInternalState(zosDatasetSpy, "dstype", DSType.PDS);
        
        // First call returns false, second returns true
        PowerMockito.doReturn(false).doReturn(true).when(zosDatasetSpy).exists();
        zosDatasetSpy.create();
        Assert.assertTrue("created() should return true", zosDatasetSpy.created());
       
        // Create retain
        PowerMockito.doReturn(false).doReturn(true).when(zosDatasetSpy).exists();
        zosDatasetSpy.createRetain();
        Assert.assertTrue("created() should return true", zosDatasetSpy.created());
        Assert.assertTrue("retainToTestEnd() should return true", zosDatasetSpy.retainToTestEnd());
       
        // Create retain temporary
        PowerMockito.doReturn(false).doReturn(true).when(zosDatasetSpy).exists();
        zosDatasetSpy.createRetainTemporary();
        Assert.assertTrue("created() should return true", zosDatasetSpy.created());
        Assert.assertTrue("retainToTestEnd() should return true", zosDatasetSpy.retainToTestEnd());
        Assert.assertTrue("isTemporary() should return true", zosDatasetSpy.isTemporary());
       
        // Create temporary
        PowerMockito.doReturn(false).doReturn(true).when(zosDatasetSpy).exists();
        zosDatasetSpy.createTemporary();
        Assert.assertTrue("created() should return true", zosDatasetSpy.created());
        Assert.assertTrue("isTemporary() should return true", zosDatasetSpy.isTemporary());
    }
    
    @Test
    public void testCreateExists() throws ZosDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" already exists on image " + IMAGE);
        
        zosDatasetSpy.create();
    }
    
    @Test
    public void testCreateRseapiException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosDatasetSpy.create();
    }
    
    @Test
    public void testCreateBadHttpResponseException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error Create data set, HTTP Status Code 404 : NOT_FOUND");
        
        zosDatasetSpy.create();
    }

    @Test
    public void testDelete() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        PowerMockito.doReturn(true).doReturn(false).when(zosDatasetSpy).exists();
        PowerMockito.doNothing().when(zosDatasetSpy).delete(Mockito.any());
        Assert.assertTrue("delete() should return true", zosDatasetSpy.delete());
        
        PowerMockito.doReturn(true).doReturn(true).when(zosDatasetSpy).exists();        
        Assert.assertFalse("delete() should return false", zosDatasetSpy.delete());
    }
    
    @Test
    public void testDeleteNotExists() throws ZosDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosDatasetSpy.delete();        
    }
    
    @Test
    public void testExists() throws ZosDatasetException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject responseBody = new JsonObject();        
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);        
        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());

        JsonArray itemsArray = new JsonArray();
        responseBody.add("items", itemsArray);        
        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
        
        JsonObject item = new JsonObject();
        item.addProperty("name", "ANOTHER.DATASET.NAME");
        itemsArray.add(item);
        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
        
        item.addProperty("name", DATASET_NAME);
        Assert.assertTrue("exists() should return true", zosDatasetSpy.exists());
    }

    @Test
    public void testExistsRseapiException() throws ZosDatasetException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
    
        zosDatasetSpy.exists();
    }

    @Test
    public void testExistsBadHttpResponseException() throws ZosDatasetException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error List data set, HTTP Status Code 404 : NOT_FOUND");
    
        zosDatasetSpy.exists();
    }

    @Test
    public void testExistsRseapiResponseException() throws ZosDatasetException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to list data set \"" + DATASET_NAME + "\" on image " + IMAGE);
    
        zosDatasetSpy.exists();
    }

    @Test
    public void testStoreText() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        PowerMockito.doNothing().when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        zosDatasetSpy.storeText(CONTENT);
        
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" is a partitioned data data set");
        
        zosDatasetSpy.storeText(CONTENT);
    }

    @Test
    public void testStoreBinary() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        PowerMockito.doNothing().when(zosDatasetSpy).storeBinary(Mockito.any(), Mockito.any(), Mockito.anyBoolean());        
        zosDatasetSpy.storeBinary(CONTENT.getBytes());
        
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" is a partitioned data data set");
        zosDatasetSpy.storeBinary(CONTENT.getBytes());
    }

    @Test
    public void testRetrieveAsText() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieveAsText());
        
        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieveAsText());
        
        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieveAsText());
        
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" is a partitioned data data set. Use retrieve(String memberName) method instead");
        
        zosDatasetSpy.retrieveAsText();
    }

    @Test
    public void testRetrieveAsBinary() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("retrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.retrieveAsBinary()));

        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("retrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.retrieveAsBinary()));

        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());        
        Assert.assertEquals("retrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.retrieveAsBinary()));
        
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" is a partitioned data data set. Use retrieve(String memberName) method instead");
        
        zosDatasetSpy.retrieveAsBinary();
    }
    
    @Test
    public void testSaveToResultsArchive() throws IOException, ZosManagerException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        PowerMockito.doNothing().when(zosDatasetSpy).savePDSToResultsArchive();
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
        RseapiZosFileManagerImpl.setDatasetArtifactRoot(newMockedPath(false));
        RseapiZosFileManagerImpl.setCurrentTestMethodArchiveFolderName("testMethod");
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieveAsText();
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieveAsBinary();
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
        
        logMessage = null;
        String expectedMessage = "\"" + DATASET_NAME + "\"" + " archived to " + PATH_MOCK;
        zosDatasetSpy.saveToResultsArchive();
		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);

        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
        logMessage = null;
        zosDatasetSpy.saveToResultsArchive();
		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);

        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        logMessage = null;
        expectedMessage = null;
        zosDatasetSpy.saveToResultsArchive();
		Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);

        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        logMessage = null;
        expectedMessage = null;
        zosDatasetSpy.saveToResultsArchive();
        Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
        
        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).exists();
        logMessage = null;
        expectedMessage = "Unable to save data set to archive";
        zosDatasetSpy.saveToResultsArchive();
        Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
        
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
        zosDatasetSpy.saveToResultsArchive();
        Assert.assertEquals("saveToResultsArchive() should log specified message", expectedMessage, logMessage);
    }
    
    @Test
    public void testSavePDSToResultsArchive() throws IOException, ZosManagerException {
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
        RseapiZosFileManagerImpl.setDatasetArtifactRoot(newMockedPath(false));
        RseapiZosFileManagerImpl.setCurrentTestMethodArchiveFolderName("testMethod");
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
        
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        Collection<String> datasetMembers = new ArrayList<>();
        PowerMockito.doReturn(datasetMembers).when(zosDatasetSpy).memberList();
        logMessage = null;
        String expectedMessage = null;
        zosDatasetSpy.savePDSToResultsArchive();
		Assert.assertEquals("savePDSToResultsArchive() should log specified message", expectedMessage, logMessage);
		
        datasetMembers.add(MEMBER_NAME);
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
        logMessage = null;
        expectedMessage = "\"" + DATASET_NAME + "(" + MEMBER_NAME + ")\" archived to " + PATH_MOCK;
        zosDatasetSpy.savePDSToResultsArchive();
		Assert.assertEquals("savePDSToResultsArchive() should log specified message", expectedMessage, logMessage);
		
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
        logMessage = null;
        zosDatasetSpy.savePDSToResultsArchive();
		Assert.assertEquals("savePDSToResultsArchive() should log specified message", expectedMessage, logMessage);
		
		PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());       
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        zosDatasetSpy.savePDSToResultsArchive();
    }
    
    private Path newMockedPath(boolean fileExists) throws IOException {
        Path pathMock = Mockito.mock(Path.class);
        Mockito.when(pathMock.toString()).thenReturn(PATH_MOCK);
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
    
    @Test
    public void testIsPDS() throws ZosDatasetException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dataSetOrganization", "PO");
        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
        Assert.assertTrue("isPDS() should return true", zosDatasetSpy.isPDS());
        
        jsonObject.addProperty("dataSetOrganization", "PS");
        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
        Assert.assertFalse("isPDS() should return false", zosDatasetSpy.isPDS());
    }
    
    @Test
    public void testMemberCreate() throws ZosDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doNothing().when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        zosDatasetSpy.memberCreate(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        
        zosDatasetSpy.memberCreate(MEMBER_NAME);
    }
    
    @Test
    public void testMemberDelete() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        zosDatasetSpy.memberDelete(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).exists();
        
        Mockito.clearInvocations(zosDatasetSpy);
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(false).when(zosDatasetSpy).memberExists(Mockito.any());
        zosDatasetSpy.memberDelete(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).memberExists(Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
        PowerMockito.doNothing().when(zosDatasetSpy).delete(Mockito.any());
        zosDatasetSpy.memberDelete(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(2)).memberExists(Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        PowerMockito.doReturn(true).doReturn(false).when(zosDatasetSpy).memberExists(Mockito.any());        
        zosDatasetSpy.memberDelete(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(2)).memberExists(Mockito.any());
    }
    
    @Test
    public void testMemberDeleteNoPDS() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");

        zosDatasetSpy.memberDelete(MEMBER_NAME);
    }

    @Test
    public void testMemberExists() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        
        ArrayList<String> datasetMembers = new ArrayList<>();
        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", datasetMembers);
        PowerMockito.doReturn(datasetMembers).when(zosDatasetSpy).memberList();
        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));        

        datasetMembers.add(MEMBER_NAME);
        Assert.assertTrue("memberExists() should return true", zosDatasetSpy.memberExists(MEMBER_NAME));
        
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject =  responseBody;
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);        
//        Assert.assertTrue("memberExists() should return true", zosDatasetSpy.memberExists(MEMBER_NAME));        
//        
//        jsonObject.add("items", getJsonArray("", "REBMEM", 1, 0));
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);        
//        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));
//        
//        
//        jsonObject = getJsonObject(2);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);        
//        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));
    }
    
    @Test
    public void testMemberExistsNotPDS() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        
        zosDatasetSpy.memberExists(MEMBER_NAME);
    }
    
//    @Test
//    public void testMemberExistsRseapiException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
//
//        exceptionRule.expect(ZosDatasetException.class);
//        exceptionRule.expectMessage(EXCEPTION);
//        
//        zosDatasetSpy.memberExists(MEMBER_NAME);
//    }
    
//    @Test
//    public void testMemberExistsBadHttpResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        JsonObject jsonObject =  responseBody;
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
//    
//        exceptionRule.expect(ZosDatasetException.class);
//        exceptionRule.expectMessage(ERROR);
//        
//        zosDatasetSpy.memberExists(MEMBER_NAME);
//    }

//    @Test
//    public void testMemberExistsRseapiResponseException() throws ZosDatasetException, RseapiException {
//        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
//        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
//        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
//    
//        exceptionRule.expect(ZosDatasetException.class);
//        exceptionRule.expectMessage("Unable to list members of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
//        
//        zosDatasetSpy.memberExists(MEMBER_NAME);
//    }

    @Test
    public void testMemberStoreText() throws ZosDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doNothing().when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        zosDatasetSpy.memberStoreText(MEMBER_NAME, CONTENT);
        
        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).storeText(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.memberStoreText(MEMBER_NAME, CONTENT);
    }

    @Test
    public void testMemberStoreTextNotPDS() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        
        zosDatasetSpy.memberStoreText(MEMBER_NAME, CONTENT);
    }

    @Test
    public void testMemberStoreBinary() throws ZosDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doNothing().when(zosDatasetSpy).storeBinary(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        zosDatasetSpy.memberStoreBinary(MEMBER_NAME, CONTENT.getBytes());
        
        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).storeBinary(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.memberStoreBinary(MEMBER_NAME, CONTENT.getBytes());
    }

    @Test
    public void testMemberStoreBinaryNotPDS() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        
        zosDatasetSpy.memberStoreBinary(MEMBER_NAME, CONTENT.getBytes());
    }

    @Test
    public void testMemberRetrieveAsText() throws ZosDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());        
        Assert.assertEquals("memberRetrieveText() should return the supplied value", CONTENT, zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME));

        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("memberRetrieveText() should return the supplied value", CONTENT, zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME));

        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());
        Assert.assertEquals("memberRetrieveText() should return the supplied value", CONTENT, zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME));
        
        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).retrieve(Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME);
    }

    @Test
    public void testMemberRetrieveAsTextNotPDS() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        
        zosDatasetSpy.memberRetrieveAsText(MEMBER_NAME);
    }

    @Test
    public void testMemberRetrieveAsBinary() throws ZosDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("memberRetrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME)));
        
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());       
        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("memberRetrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME)));
        
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieve(Mockito.any());
        Assert.assertEquals("memberRetrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME)));
        
        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetSpy).retrieve(Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME);
    }

    @Test
    public void testMemberRetrieveAsBinaryNotPDS() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        
        zosDatasetSpy.memberRetrieveAsBinary(MEMBER_NAME);
    }     
    
    @Test
    public void testMemberList() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);        
        Collection<String> memberList = zosDatasetSpy.memberList();
        Assert.assertEquals("memberlist() should return a list with 0 member", listOfMembers(0), memberList);        
        
        JsonObject responseBody = new JsonObject();
        JsonArray items = new JsonArray();
        items.add("MEMBER");
        responseBody.add("items", items);        
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);        
        memberList = zosDatasetSpy.memberList();
        Assert.assertEquals("memberlist() should return a list with 1 member", listOfMembers(1), memberList);

        items.add("MEMBER1");
        memberList = zosDatasetSpy.memberList();
        Assert.assertEquals("memberlist() should return a list with 2 members", listOfMembers(2), memberList);
    }
    
    @Test
    public void testMemberListNoPDSException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");

        zosDatasetSpy.memberList();
    }
    
    @Test
    public void testMemberListRseapiException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosDatasetSpy.memberList();
    }

    @Test
    public void testMemberListBadHttpResponseException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("BAD_REQUEST");
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error List data set members, HTTP Status Code 400 : BAD_REQUEST");
        zosDatasetSpy.memberList();
    }

    @Test
    public void testMemberListRseapiResponseException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve member list of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        zosDatasetSpy.memberList();
    }

    @Test
    public void testMemberSaveToTestArchive() throws IOException, ZosManagerException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn("PATH_NAME").when(zosManagerMock).buildUniquePathName(Mockito.any(), Mockito.any());
        RseapiZosFileManagerImpl.setDatasetArtifactRoot(newMockedPath(false));
        RseapiZosFileManagerImpl.setCurrentTestMethodArchiveFolderName("testMethod");
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        
        logMessage = null;
        String expectedMessage = "\"" + DATASET_NAME + "(" + MEMBER_NAME + ")\"" + " archived to " + PATH_MOCK;
        zosDatasetSpy.memberSaveToTestArchive(MEMBER_NAME);
        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);

        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
        logMessage = null;
        zosDatasetSpy.memberSaveToTestArchive(MEMBER_NAME);
        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);

        PowerMockito.doThrow(new ZosManagerException(EXCEPTION)).when(zosManagerMock).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
        logMessage = null;
        expectedMessage = "Unable to save data set member to archive";
        zosDatasetSpy.memberSaveToTestArchive(MEMBER_NAME);
        Assert.assertEquals("memberSaveToTestArchive() should log specified message", expectedMessage, logMessage);

        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        zosDatasetSpy.memberSaveToTestArchive(MEMBER_NAME);
    }
    
    @Test
    public void testSetDataType() {
        DatasetDataType value = DatasetDataType.TEXT;
        zosDatasetSpy.setDataType(value);
        Assert.assertEquals("testDataType() should return the supplied value", value, zosDatasetSpy.getDataType());
        value = DatasetDataType.BINARY;
        
        zosDatasetSpy.setDataType(value);
        Assert.assertEquals("testDataType() should return the supplied value", value, zosDatasetSpy.getDataType());
    }
    
    @Test
    public void testUnit() {
        Assert.assertNull("getUnit() should return null", zosDatasetSpy.getUnit());
        String value = "UNIT";
        zosDatasetSpy.setUnit(value);
        Assert.assertEquals("getUnit() should return the supplied value", value, zosDatasetSpy.getUnit());
    }
    
    @Test
    public void testVolumes() {
        Assert.assertNull("getVolumes() should return null", zosDatasetSpy.getVolumes());
        String value = "VOLUMES";
        zosDatasetSpy.setVolumes(value);
        Assert.assertEquals("getVolumes() should return the supplied value", value, zosDatasetSpy.getVolumes());
    }
    
    @Test
    public void testDatasetOrganization() {
        Assert.assertNull("getDatasetOrganization() should return null", zosDatasetSpy.getDatasetOrganization());
        DatasetOrganization value = DatasetOrganization.SEQUENTIAL;
        zosDatasetSpy.setDatasetOrganization(value);
        Assert.assertEquals("getDatasetOrganization() should return the supplied value", value, zosDatasetSpy.getDatasetOrganization());
    }
    
    @Test
    public void testSpace() {
        Assert.assertNull("getSpace() should return null", zosDatasetSpy.getSpaceUnit());
        Assert.assertEquals("getPrimaryExtents() should return -1", -1, zosDatasetSpy.getPrimaryExtents());
        Assert.assertEquals("getSecondaryExtents() should return -1", -1, zosDatasetSpy.getSecondaryExtents());
        SpaceUnit spaceUnit = SpaceUnit.CYLINDERS;
        int primaryExtents = 99;
        int secondaryExtents = 99;
        zosDatasetSpy.setSpace(spaceUnit, primaryExtents, secondaryExtents);
        Assert.assertEquals("getSpaceUnit() should return the supplied value", spaceUnit, zosDatasetSpy.getSpaceUnit());
        Assert.assertEquals("getPrimaryExtents() should return the supplied value", primaryExtents, zosDatasetSpy.getPrimaryExtents());
        Assert.assertEquals("getSecondaryExtents() should return the supplied value", secondaryExtents, zosDatasetSpy.getSecondaryExtents());
    }
    
    @Test
    public void testDirectoryBlocks() {
        Assert.assertEquals("getDirectoryBlocks() should return -1", -1, zosDatasetSpy.getDirectoryBlocks());
        int value = 99;
        zosDatasetSpy.setDirectoryBlocks(value);
        Assert.assertEquals("getDirectoryBlocks() should return the supplied value", value, zosDatasetSpy.getDirectoryBlocks());
    }
    
    @Test
    public void testRecordFormat() {
        Assert.assertNull("getRecordFormat() should return null", zosDatasetSpy.getRecordFormat());
        RecordFormat value = RecordFormat.FIXED;
        zosDatasetSpy.setRecordFormat(value);
        Assert.assertEquals("getRecordFormat() should return the supplied value", value, zosDatasetSpy.getRecordFormat());
    }
    
    @Test
    public void testBlockSize() {
        Assert.assertEquals("getBlockSize() should return -1", -1, zosDatasetSpy.getBlockSize());
        int value = 99;
        zosDatasetSpy.setBlockSize(value);
        Assert.assertEquals("getBlockSize() should return the supplied value", value, zosDatasetSpy.getBlockSize());
    }
    
    @Test
    public void testRecordlength() {
        Assert.assertEquals("getRecordlength() should return -1", -1, zosDatasetSpy.getRecordlength());
        int value = 99;
        zosDatasetSpy.setRecordlength(value);
        Assert.assertEquals("getRecordlength() should return the supplied value", value, zosDatasetSpy.getRecordlength());
    }
    
    @Test
    public void testManagementClass() {
        Assert.assertNull("getManagementClass() should return null", zosDatasetSpy.getManagementClass());
        String value = "MANAGEMENTCLASS";
        
        zosDatasetSpy.setManagementClass(value);        
        Assert.assertEquals("getManagementClass() should return the supplied value", value, zosDatasetSpy.getManagementClass());
    }
    
    @Test
    public void testStorageClass() {
        Assert.assertNull("getStorageClass() should return null", zosDatasetSpy.getStorageClass());
        String value = "STORAGECLASS";
        
        zosDatasetSpy.setStorageClass(value);
        Assert.assertEquals("getStorageClass() should return the supplied value", value, zosDatasetSpy.getStorageClass());
    }
    
    @Test
    public void testDataClass() {
        Assert.assertNull("getDataClass() should return null", zosDatasetSpy.getDataClass());
        String value = "DATACLASS";
        
        zosDatasetSpy.setDataClass(value);
        Assert.assertEquals("getDataClass() should return the supplied value", value, zosDatasetSpy.getDataClass());
    }
    
    @Test
    public void testDatasetType() {
        Assert.assertNull("getDatasetType() should return null", zosDatasetSpy.getDatasetType());
        
        zosDatasetSpy.setDatasetType(DSType.BASIC);
        Assert.assertEquals("getDatasetType() should return DSType.BASIC", DSType.BASIC, zosDatasetSpy.getDatasetType());
    }
    
    @Test
    public void testExtents() {
        int value = 99;
        Whitebox.setInternalState(zosDatasetSpy, "extents", value);
        Assert.assertEquals("getExtents() should return expected value", value, zosDatasetSpy.getExtents());
    }
    
    @Test
    public void testUsed() {
        int value = 99;
        Whitebox.setInternalState(zosDatasetSpy, "used", value);
        Assert.assertEquals("getExtents() should return expected value", value, zosDatasetSpy.getUsed());
    }
    
    @Test
    public void testCreateDate() {
        String value = "01/01/2000";
        Whitebox.setInternalState(zosDatasetSpy, "createDate", value);
        Assert.assertEquals("getCreateDate() should return expected value", value, zosDatasetSpy.getCreateDate());
    }
    
    @Test
    public void testReferencedDate() {
        String value = "01/01/2000";
        Whitebox.setInternalState(zosDatasetSpy, "referencedDate", value);
        Assert.assertEquals("getReferencedDate() should return expected value", value, zosDatasetSpy.getReferencedDate());
    }
    
    @Test
    public void testExpirationDate() {
        String value = "01/01/2000";
        Whitebox.setInternalState(zosDatasetSpy, "expirationDate", value);
        Assert.assertEquals("getExpirationDate() should return expected value", value, zosDatasetSpy.getExpirationDate());
    }
    
    @Test
    public void testGetName() {
        Assert.assertEquals("getName() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.getName());
        Assert.assertEquals("toString() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.toString());
    }
    
    @Test
    public void testGetAttibutesAsString() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        JsonObject jsonObject = new JsonObject();
        PowerMockito.doReturn(jsonObject).when(zosDatasetSpy).getAttibutes();
        StringBuilder attributes = new StringBuilder();
        attributes.append("Data Set Name=,");
        attributes.append("Volume serial=,");
        attributes.append("Organization=,");
        attributes.append("Record format=,");
        attributes.append("Record length=,");
        attributes.append("Block size=,");
        attributes.append("Data set type=,");
        attributes.append("Allocated extents=,");
        attributes.append("PDS=false,");
        attributes.append("Creation date=,");
        attributes.append("Referenced date=");
        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());

        jsonObject.addProperty("dataSetOrganization", "PO");
        attributes = new StringBuilder();
        attributes.append("Data Set Name=,");
        attributes.append("Volume serial=,");
        attributes.append("Organization=PO,");
        attributes.append("Record format=,");
        attributes.append("Record length=,");
        attributes.append("Block size=,");
        attributes.append("Data set type=,");
        attributes.append("Allocated extents=,");
        attributes.append("PDS=true,");
        attributes.append("Creation date=,");
        attributes.append("Referenced date=");
        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());
    }
    
    @Test
    public void testGetAttibutesAsStringNotExist() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
    
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosDatasetSpy.getAttibutesAsString();        
    }
    
    @Test
    public void testGetAttibutes() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        JsonArray items = new JsonArray();
        JsonObject attributes = new JsonObject();
        attributes.addProperty("name", DATASET_NAME);
        items.add(attributes);
        responseBody.add("items", items);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    	Assert.assertEquals("getAttibutes() should return the expected value", attributes, zosDatasetSpy.getAttibutes());
    }
    
    @Test
    public void testGetAttibutesNotExistException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
    	zosDatasetSpy.getAttibutes();
    }
    
    @Test
    public void testGetAttibutesRseapiException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
    	zosDatasetSpy.getAttibutes();
    }
    
    @Test
    public void testGetAttibutesBadHttpResponseException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error list data set, HTTP Status Code 404 : NOT_FOUND");
    	zosDatasetSpy.getAttibutes();
    }
    
    @Test
    public void testGetAttibutesGetContentException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException());
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable list to attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
    	zosDatasetSpy.getAttibutes();
    }
    
    @Test
    public void testGetAttibutesException1() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
    	zosDatasetSpy.getAttibutes();
    }
    
    @Test
    public void testGetAttibutesException2() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        JsonArray items = new JsonArray();
        responseBody.add("items", items);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
    	zosDatasetSpy.getAttibutes();
    }
    
    @Test
    public void testGetAttibutesException3() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        JsonArray items = new JsonArray();
        JsonObject attributes = new JsonObject();
        attributes.addProperty("no name", DATASET_NAME);
        items.add(attributes);
        responseBody.add("items", items);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
    	zosDatasetSpy.getAttibutes();
    }
    
    @Test
    public void testGetAttibutesException4() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        JsonArray items = new JsonArray();
        JsonObject attributes = new JsonObject();
        attributes.addProperty("name", "ANOTHER.DATASET.NAME");
        items.add(attributes);
        responseBody.add("items", items);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
    	zosDatasetSpy.getAttibutes();
    }
    
    
    
    
    @Test
    public void testRetrieveAttibutes() throws ZosDatasetException {
        Whitebox.setInternalState(zosDatasetSpy, "rseapiZosDatasetAttributesListdsi", zosDatasetAttributesListdsiMock);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("listdsirc", 0);
        Mockito.when(zosDatasetAttributesListdsiMock.get(Mockito.anyString())).thenReturn(jsonObject);
        PowerMockito.doNothing().when(zosDatasetSpy).setAttributes(Mockito.any());
        zosDatasetSpy.retrieveAttibutes();
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).setAttributes(Mockito.any());
        PowerMockito.doReturn("MESSAGE").when(zosDatasetSpy).emptyStringWhenNull(Mockito.any(),Mockito.any());
        
        jsonObject.addProperty("listdsirc", 4);
        zosDatasetSpy.retrieveAttibutes();
        Mockito.verify(zosDatasetSpy, Mockito.times(2)).setAttributes(Mockito.any());
        
        jsonObject.addProperty("sysreason", 12);
        zosDatasetSpy.retrieveAttibutes();
        String expectedMessage = "Unable to get full attributes for data set \"" + DATASET_NAME + "\". LISTDSI RC=4\n" + 
                "SYSREASON=12\n" + 
                "SYSMSGLVL1: MESSAGE\n" + 
                "SYSMSGLVL2: MESSAGE";
        Assert.assertEquals("retrieveAttibutes() should log specified message", expectedMessage , logMessage);
        
        jsonObject.addProperty("listdsirc", 12);
        expectedMessage = "Unable to get attributes for data set \"" + DATASET_NAME + "\". LISTDSI RC=12\n" + 
                "SYSREASON=12\n" + 
                "SYSMSGLVL1: MESSAGE\n" + 
                "SYSMSGLVL2: MESSAGE";
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(expectedMessage);
        zosDatasetSpy.retrieveAttibutes();
    }
    
    @Test
    public void testSetAttributes() {
        JsonObject jsonObject = new JsonObject();
        zosDatasetSpy.setAttributes(jsonObject);
        
        jsonObject.addProperty("volumeSerial", "VOLSER");
        jsonObject.addProperty("unit", "UNIT");
        jsonObject.addProperty("dataSetOrganization", "PO");
        jsonObject.addProperty("allocationUnit", "CYLINDER");
        jsonObject.addProperty("primary", "1");
        jsonObject.addProperty("secondary", 2);
        jsonObject.addProperty("directoryBlocks", 3);
        jsonObject.addProperty("blockSize", 4);
        jsonObject.addProperty("recordFormat", "FB");
        jsonObject.addProperty("recordLength", 5);
        jsonObject.addProperty("dataClass", "DATACLASS");
        jsonObject.addProperty("storClass", "STORECLASS");
        jsonObject.addProperty("mgmtClass", "MGNTCLASS");        
        jsonObject.addProperty("dsnType", "PDSE");
        jsonObject.addProperty("used", 6);
        jsonObject.addProperty("extents", 7);
        jsonObject.addProperty("creationDate", "CDATE");
        jsonObject.addProperty("referenceDate", "RDATE");
        jsonObject.addProperty("expiryDate", "EDATE");
        zosDatasetSpy.setAttributes(jsonObject);

        Assert.assertEquals("setAttributes() should set supplied value", "VOLSER", zosDatasetSpy.getVolumes());
        Assert.assertEquals("setAttributes() should set supplied value", "UNIT", zosDatasetSpy.getUnit());
        Assert.assertEquals("setAttributes() should set supplied value", DatasetOrganization.PARTITIONED, zosDatasetSpy.getDatasetOrganization());
        Assert.assertEquals("setAttributes() should set supplied value", SpaceUnit.CYLINDERS, zosDatasetSpy.getSpaceUnit());
        Assert.assertEquals("setAttributes() should set supplied value", 1, zosDatasetSpy.getPrimaryExtents());
        Assert.assertEquals("setAttributes() should set supplied value", 2, zosDatasetSpy.getSecondaryExtents());
        Assert.assertEquals("setAttributes() should set supplied value", 3, zosDatasetSpy.getDirectoryBlocks());
        Assert.assertEquals("setAttributes() should set supplied value", 4, zosDatasetSpy.getBlockSize());
        Assert.assertEquals("setAttributes() should set supplied value", RecordFormat.FIXED_BLOCKED, zosDatasetSpy.getRecordFormat());
        Assert.assertEquals("setAttributes() should set supplied value", 5, zosDatasetSpy.getRecordlength());
        Assert.assertEquals("setAttributes() should set supplied value", DSType.PDSE, zosDatasetSpy.getDatasetType());
        Assert.assertEquals("setAttributes() should set supplied value", 6, zosDatasetSpy.getUsed());
        Assert.assertEquals("setAttributes() should set supplied value", 7, zosDatasetSpy.getExtents());
        Assert.assertEquals("setAttributes() should set supplied value", "CDATE", zosDatasetSpy.getCreateDate());
        Assert.assertEquals("setAttributes() should set supplied value", "RDATE", zosDatasetSpy.getReferencedDate());
        Assert.assertEquals("setAttributes() should set supplied value", "EDATE", zosDatasetSpy.getExpirationDate());
        Assert.assertEquals("setAttributes() should set supplied value", "DATACLASS", zosDatasetSpy.getDataClass());
        Assert.assertEquals("setAttributes() should set supplied value", "STORECLASS", zosDatasetSpy.getStorageClass());
        Assert.assertEquals("setAttributes() should set supplied value", "MGNTCLASS", zosDatasetSpy.getManagementClass());
        
        jsonObject.addProperty("dsnType", "DATA_LIBRARY");
        zosDatasetSpy.setAttributes(jsonObject);
        Assert.assertEquals("setAttributes() should set supplied value", DSType.LIBRARY, zosDatasetSpy.getDatasetType());
    }
    
    @Test
    public void testInputStreamToByteArray() throws ZosDatasetException, IOException {
        ByteArrayInputStream contentIs = new ByteArrayInputStream(CONTENT.getBytes());
        Assert.assertArrayEquals("inputStreamToByteArray() should return the supplied value", CONTENT.getBytes(), zosDatasetSpy.inputStreamToByteArray(contentIs));
        
        ByteArrayInputStream contentIsSpy = Mockito.spy(contentIs);
        PowerMockito.doThrow(new IOException(EXCEPTION)).when(contentIsSpy).read(Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Failed to collect binary");
        
        zosDatasetSpy.inputStreamToByteArray(contentIsSpy);
    }
    
    @Test
    public void testInternalRetrieve() throws ZosDatasetException, RseapiException {
    	zosDatasetSpy.setDataType(DatasetDataType.TEXT);    
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject responseBody = new JsonObject();
		Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Assert.assertEquals("retrieve() should return the supplied value", "", zosDatasetSpy.retrieve(null));

        responseBody.addProperty("records", CONTENT);
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));
        zosDatasetSpy.setDataType(DatasetDataType.BINARY);
        
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));
    }
    
    @Test
    public void testInternalRetrieveRseapiException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
    
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.retrieve(null);
    }
    
    @Test
    public void testInternalRetrieveBadHttpResponseException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error retrieve content of data set, HTTP Status Code 404 : NOT_FOUND");
        
        zosDatasetSpy.retrieve(null);
    }

    @Test
    public void testInternalRetrieveRseapiResponseException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve content of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.retrieve(null);
        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
    }
    
    @Test
    public void testInternalDelete() throws ZosDatasetException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
        zosDatasetSpy.delete(DATASET_NAME);
        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
    }
    
    @Test
    public void testInternalDeleteResponseException() throws ZosDatasetException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        zosDatasetSpy.delete(DATASET_NAME);
    }
    
    @Test
    public void testInternalInvalidHttpResponseDelete() throws ZosDatasetException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error delete DATA.SET.NAME, HTTP Status Code 404 : NOT_FOUND");
        zosDatasetSpy.delete(DATASET_NAME);
    }
    
    @Test
    public void testInternalStoreText() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
        
        Mockito.clearInvocations(rseapiResponseMock);        
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
        Mockito.verify(rseapiResponseMock, Mockito.times(2)).getStatusCode();        
        Mockito.clearInvocations(zosDatasetSpy);
    }
    
    @Test
    public void testInternalStoreTextNotExist() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreTextRseapiException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreTextBadHttpResponseException() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error writing to data set, HTTP Status Code 404 : NOT_FOUND");
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreBinary() throws ZosDatasetException, RseapiException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
        Mockito.verify(rseapiResponseMock, Mockito.times(1)).getStatusCode();
        
        Mockito.clearInvocations(rseapiResponseMock);        
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
        Mockito.verify(rseapiResponseMock, Mockito.times(2)).getStatusCode();        
        Mockito.clearInvocations(zosDatasetSpy);
    }
    
    @Test
    public void testInternalStoreBinaryNotExist() throws ZosDatasetException, RseapiException {  
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreBinaryRseapiException() throws ZosDatasetException, RseapiException { 
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreBinaryBadHttpResponseException() throws ZosDatasetException, RseapiException { 
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Error write to data set, HTTP Status Code 404 : NOT_FOUND");
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
    }

    @Test
    public void testAddPropertyWhenSet() throws ZosDatasetException {
        JsonObject responseBody = new JsonObject();
        JsonObject returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", null);
        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", responseBody, returnedJsonObject);
        
        responseBody = new JsonObject();
        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", "value");
        Assert.assertEquals("testAddPropertyWhenSet() should return the correct String property value", "value", returnedJsonObject.get("property").getAsString());

        responseBody = new JsonObject();
        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", -1);
        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", responseBody, returnedJsonObject);
        
        responseBody = new JsonObject();
        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(responseBody, "property", 99);
        Assert.assertEquals("testAddPropertyWhenSet() should return the correct int property value", 99, returnedJsonObject.get("property").getAsInt());

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Invlaid type of \"" + DummyClass.class.getName() + "\" for property \"property\" on image " + IMAGE);
        responseBody = new JsonObject();
        zosDatasetSpy.addPropertyWhenSet(responseBody, "property", new DummyClass());
    }
    
    //TODO    @Test
    public void testGetMembers() {
        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("returnedRows", 0);
        //TODO        Assert.assertFalse("getMembers() should return false", zosDatasetSpy.getMembers(jsonObject));
        Assert.assertEquals("datasetMembers should return a list with 0 members", listOfMembers(0), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
        
        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
        jsonObject = new JsonObject();
        //TODO        Assert.assertFalse("getMembers() should return false", zosDatasetSpy.getMembers(jsonObject));
        Assert.assertEquals("datasetMembers should return a list with 1 member", listOfMembers(1), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
        
        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
        jsonObject = new JsonObject();
        jsonObject.addProperty("moreRows", true);
        //TODO        Assert.assertTrue("getMembers() should return true", zosDatasetSpy.getMembers(jsonObject));
        Assert.assertEquals("datasetMembers should return a list with 2 members", listOfMembers(2), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
    }
    
    @Test
    public void testBuildErrorString() throws RseapiException {
    	Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    	Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("OK");
        String expectedString = "Error action, HTTP Status Code 200 : OK";
        String returnString = RseapiZosDatasetImpl.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        JsonObject responseBody = new JsonObject();
        responseBody.addProperty("status", "status");
        responseBody.addProperty("message", "message");
    	Mockito.when(rseapiResponseMock.getContent()).thenReturn(responseBody);
        expectedString = "Error action, HTTP Status Code 200 : OK\n" +
        				 "status: status\n" +
        				 "message: message";
        returnString = RseapiZosDatasetImpl.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        Mockito.when(rseapiResponseMock.getContent()).thenReturn("message");
        expectedString = "Error action, HTTP Status Code 200 : OK response body:\n" +
        				 "message";
        returnString = RseapiZosDatasetImpl.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        Mockito.when(rseapiResponseMock.getContent()).thenReturn(0);
        expectedString = "Error action, HTTP Status Code 200 : OK";
        returnString = RseapiZosDatasetImpl.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        Mockito.when(rseapiResponseMock.getContent()).thenThrow(new RseapiException());
        expectedString = "Error action, HTTP Status Code 200 : OK";
        returnString = RseapiZosDatasetImpl.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
    }
        
    @Test
    public void testSplitDSN() {
        zosDatasetSpy.splitDSN(DATASET_NAME);
        Assert.assertEquals("getName() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.getName());
        zosDatasetSpy.splitDSN(DATASET_NAME + "(" + MEMBER_NAME + ")");
        Assert.assertEquals("getName() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.getName());
    }
    
    @Test
    public void testGetRseapiApiProcessor() {
        Assert.assertEquals("getRseapiApiProcessor() should return the mocked IRseapiRestApiProcessor", rseapiApiProcessorMock, zosDatasetSpy.getRseapiApiProcessor());
    }

    private Collection<String> listOfMembers(int count) {
        Collection<String> memberList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                memberList.add(MEMBER_NAME);
            } else {
                memberList.add(MEMBER_NAME + i);
            }
        }
        return memberList;
    }
    
    class DummyClass {
        @Override
        public String toString() {
            throw new NotImplementedException("Not Implemented");
        }
    }
}
