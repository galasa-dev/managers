/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpStatus;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetDataType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.zosmf.manager.internal.properties.RestrictZosmfToImage;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestrictZosmfToImage.class})
public class TestZosDatasetImpl {
    
    private ZosDatasetImpl zosDataset;
    
    private ZosDatasetImpl zosDatasetSpy;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private ZosmfManagerImpl zosmfManagerMock;
    
    @Mock
    private IZosmfRestApiProcessor zosmfApiProcessorMock;
    
    @Mock
    private IZosmfResponse zosmfResponseMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String DATASET_NAME = "DATA.SET.NAME";
    
    private static final String MEMBER_NAME = "MEMBER";
    
    private static final String IMAGE = "IMAGE";
    
    private static final String CONTENT = "content";
    
    private static final String EXCEPTION = "exception";
    
    @Before
    public void setup() throws Exception {
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        
        PowerMockito.mockStatic(RestrictZosmfToImage.class);
        Mockito.when(RestrictZosmfToImage.get(Mockito.any())).thenReturn(true);

        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictZosmfToImage.get(zosImageMock.getImageID()))).thenReturn(zosmfApiProcessorMock);
        ZosFileManagerImpl.setZosmfManager(zosmfManagerMock);
        
        zosDataset = new ZosDatasetImpl(zosImageMock, DATASET_NAME);
        zosDatasetSpy = Mockito.spy(zosDataset);
    }
    
    @Test
    public void testConstructorException() throws ZosmfManagerException, ZosFileManagerException {
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictZosmfToImage.get(zosImageMock.getImageID()))).thenThrow(new ZosmfManagerException(EXCEPTION));
        new ZosDatasetImpl(zosImageMock, DATASET_NAME);
    }
    
    @Test
    public void testCreate() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();

        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
       
        zosDatasetSpy.create();        
        Assert.assertFalse("created() should return false", zosDatasetSpy.created());
       
        // First call returns false, second returns true
        PowerMockito.doReturn(false).doReturn(true).when(zosDatasetSpy).exists();
        zosDatasetSpy.create();
        Assert.assertTrue("created() should return true", zosDatasetSpy.created());
       
        // Create retain
        PowerMockito.doReturn(false).doReturn(true).when(zosDatasetSpy).exists();
        zosDatasetSpy.createRetain();
        Assert.assertTrue("created() should return true", zosDatasetSpy.created());
        Assert.assertTrue("retainToTestEnd() should return true", zosDatasetSpy.retainToTestEnd());
       
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
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" aleady exists on image " + IMAGE);
        
        zosDatasetSpy.create();
    }
    
    @Test
    public void testCreateZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosDatasetSpy.create();
    }
    
    @Test
    public void testCreateBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        
        zosDatasetSpy.create();
    }

    @Test
    public void testCreateZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to create data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.create();
    }

    @Test
    public void testDelete() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        PowerMockito.doReturn(true).doReturn(false).when(zosDatasetSpy).exists();

        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
        
        Assert.assertTrue("delete() should return true", zosDatasetSpy.delete());
        
        PowerMockito.doReturn(true).doReturn(true).when(zosDatasetSpy).exists();        
        Assert.assertFalse("delete() should return false", zosDatasetSpy.delete());
    }
    
    @Test
    public void testDeleteNotCreated() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).created();
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" not created by this test run on image " + IMAGE);
        
        zosDatasetSpy.delete();        
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
    public void testDeleteZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.delete();        
    }

    @Test
    public void testDeleteBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        
        zosDatasetSpy.delete();        
    }

    @Test
    public void testDeleteZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).created();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to delete data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.delete();        
    }

    @Test
    public void testExists() throws ZosDatasetException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject jsonObject = getJsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        
        Assert.assertTrue("exists() should return true", zosDatasetSpy.exists());
        
        jsonObject.add("items", getJsonArray("ANOTHER.DATASET.NAME", null, 1, 0));
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        
        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
        
        jsonObject = getJsonObject(2);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        
        Assert.assertFalse("exists() should return false", zosDatasetSpy.exists());
    }

    @Test
    public void testExistsZosmfException() throws ZosDatasetException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
    
        zosDatasetSpy.exists();
    }

    @Test
    public void testExistsBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
    
        zosDatasetSpy.exists();
    }

    @Test
    public void testExistsZosmfResponseException() throws ZosDatasetException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
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
        
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" is a partitioned data data set. Use retrieve(String memberName) method instead");
        
        zosDatasetSpy.retrieveAsText();
    }

    @Test
    public void testRetrieveAsBinary() throws ZosDatasetException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());
        
        Assert.assertEquals("retrieveAsBinary() should return the supplied value", CONTENT, new String(zosDatasetSpy.retrieveAsBinary()));
        
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Data set \"" + DATASET_NAME + "\" is a partitioned data data set. Use retrieve(String memberName) method instead");
        
        zosDatasetSpy.retrieveAsBinary();
    }
    
    @Test
    public void testSaveToResultsArchive() throws ZosFileManagerException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        PowerMockito.doReturn("archiveLocation").when(zosDatasetSpy).storeArtifact(Mockito.any(), Mockito.any());
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieveAsText();
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).retrieveAsBinary();
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
        Mockito.clearInvocations(zosDatasetSpy);
        zosDatasetSpy.saveToResultsArchive();
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
        zosDatasetSpy.saveToResultsArchive();
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
        zosDatasetSpy.saveToResultsArchive();
        Mockito.verify(zosDatasetSpy, Mockito.times(0)).storeArtifact(Mockito.any(), Mockito.any());

        Mockito.clearInvocations(zosDatasetSpy);
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        Collection<String> datasetMembers = new ArrayList<>();
        PowerMockito.doReturn(datasetMembers).when(zosDatasetSpy).memberList();        
        zosDatasetSpy.saveToResultsArchive();
        Mockito.verify(zosDatasetSpy, Mockito.times(0)).storeArtifact(Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.TEXT);
        datasetMembers.add(MEMBER_NAME);
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).retrieve(Mockito.any());        
        zosDatasetSpy.saveToResultsArchive();
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);      
        zosDatasetSpy.saveToResultsArchive();
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());

        Mockito.clearInvocations(zosDatasetSpy);
        PowerMockito.doThrow(new ZosFileManagerException(EXCEPTION)).when(zosDatasetSpy).storeArtifact(Mockito.any(), Mockito.any());        
        zosDatasetSpy.saveToResultsArchive();
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testIsPDS() throws ZosDatasetException {
        PowerMockito.doReturn("PDS=true").when(zosDatasetSpy).getAttibutesAsString();
        Assert.assertTrue("isPDS() should return true", zosDatasetSpy.isPDS());
        
        PowerMockito.doReturn("PDS=false").when(zosDatasetSpy).getAttibutesAsString();
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
    public void testMemberDelete() throws ZosDatasetException, ZosmfException {
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
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);        
        zosDatasetSpy.memberDelete(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(2)).memberExists(Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        PowerMockito.doReturn(true).doReturn(false).when(zosDatasetSpy).memberExists(Mockito.any());        
        zosDatasetSpy.memberDelete(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(2)).memberExists(Mockito.any());
    }
    
    @Test
    public void testMemberDeleteNoPDS() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");

        zosDatasetSpy.memberDelete(MEMBER_NAME);
    }
    
    @Test
    public void testMemberDeleteZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosDatasetSpy.memberDelete(MEMBER_NAME);
    }
    
    @Test
    public void testMemberDeleteBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);  
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        
        zosDatasetSpy.memberDelete(MEMBER_NAME);
    }

    @Test
    public void testMemberDeleteZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        PowerMockito.doReturn(true).when(zosDatasetSpy).memberExists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to delete member " + MEMBER_NAME + " from data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.memberDelete(MEMBER_NAME);
    }

    @Test
    public void testMemberExists() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject =  getJsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);        
        Assert.assertTrue("memberExists() should return true", zosDatasetSpy.memberExists(MEMBER_NAME));        
        
        jsonObject.add("items", getJsonArray("", "REBMEM", 1, 0));
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);        
        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));
        
        
        jsonObject = getJsonObject(2);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);        
        Assert.assertFalse("memberExists() should return false", zosDatasetSpy.memberExists(MEMBER_NAME));
    }
    
    @Test
    public void testMemberExistsNotPDS() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");
        
        zosDatasetSpy.memberExists(MEMBER_NAME);
    }
    
    @Test
    public void testMemberExistsZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.memberExists(MEMBER_NAME);
    }
    
    @Test
    public void testMemberExistsBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject =  getJsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
    
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        
        zosDatasetSpy.memberExists(MEMBER_NAME);
    }

    @Test
    public void testMemberExistsZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
    
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to list members of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.memberExists(MEMBER_NAME);
    }

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
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).inputStreamToByteArray(Mockito.any());       
        PowerMockito.doReturn(new ByteArrayInputStream(CONTENT.getBytes())).when(zosDatasetSpy).retrieve(Mockito.any());
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
    public void testMemberList() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject = getJsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        Collection<String> memberList = zosDatasetSpy.memberList();
        Assert.assertEquals("memberlist() should return a list with 1 member", listOfMembers(1), memberList);

        jsonObject = getJsonObject(2);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        
        memberList = zosDatasetSpy.memberList();
        Assert.assertEquals("memberlist() should return a list with 2 members", listOfMembers(2), memberList);

        jsonObject = getJsonObject(2);
        jsonObject.addProperty("moreRows", true);
        JsonObject jsonObject1 = getJsonObject(2, 2);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject).thenReturn(jsonObject1);
        
        memberList = zosDatasetSpy.memberList();
        Assert.assertEquals("memberlist() should return a list with 4 members", listOfMembers(4), memberList);
    }
    
    @Test
    public void testMemberListNoPDSException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" is not a partitioned data data set");

        zosDatasetSpy.memberList();
    }
    
    @Test
    public void testMemberListZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosDatasetSpy.memberList();
    }

    @Test
    public void testMemberListBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);  
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        zosDatasetSpy.memberList();
    }

    @Test
    public void testMemberListZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve member list of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        zosDatasetSpy.memberList();
    }

    @Test
    public void testMemberSaveToTestArchive() throws ZosFileManagerException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).isPDS();
        PowerMockito.doReturn("archiveLocation").when(zosDatasetSpy).storeArtifact(Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(CONTENT).when(zosDatasetSpy).memberRetrieveAsText(Mockito.any());
        Mockito.clearInvocations(zosDatasetSpy);
        zosDatasetSpy.memberSaveToTestArchive(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());

        Mockito.clearInvocations(zosDatasetSpy);
        Whitebox.setInternalState(zosDatasetSpy, "dataType", DatasetDataType.BINARY);
        PowerMockito.doReturn(CONTENT.getBytes()).when(zosDatasetSpy).memberRetrieveAsBinary(Mockito.any());
        zosDatasetSpy.memberSaveToTestArchive(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosDatasetSpy);
        PowerMockito.doThrow(new ZosFileManagerException(EXCEPTION)).when(zosDatasetSpy).storeArtifact(Mockito.any(), Mockito.any());
        zosDatasetSpy.memberSaveToTestArchive(MEMBER_NAME);
        Mockito.verify(zosDatasetSpy, Mockito.times(1)).storeArtifact(Mockito.any(), Mockito.any());
        
        PowerMockito.doReturn(false).when(zosDatasetSpy).isPDS();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
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
        Assert.assertEquals("getDatasetOrganization() should return the supplied value", value.toString(), zosDatasetSpy.getDatasetOrganization());
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
        Assert.assertEquals("getSpaceUnit() should return the supplied value", spaceUnit.toString(), zosDatasetSpy.getSpaceUnit());
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
        Assert.assertEquals("getRecordFormat() should return the supplied value", value.toString(), zosDatasetSpy.getRecordFormat());
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
    public void testGetName() {
        Assert.assertEquals("getName() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.getName());
        Assert.assertEquals("toString() should return DATASET_NAME", DATASET_NAME, zosDatasetSpy.toString());
    }
    
    @Test
    public void testGetAttibutesAsString() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject jsonObject = getJsonObject();
        jsonObject.addProperty("dsorg", "PO");
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        StringBuffer attributes = new StringBuffer();
        attributes.append("Data Set Name=");
        attributes.append(DATASET_NAME);
        attributes.append(",Volume serial=,");
        attributes.append("Organization=,");
        attributes.append("Record format=,");
        attributes.append("Record length=,");
        attributes.append("Block size=,");
        attributes.append("Data set name type=,");
        attributes.append("Allocated extents=,");
        attributes.append("% Utilized=,");
        attributes.append("PDS=false,");
        attributes.append("Creation date=,");
        attributes.append("Referenced date=,");
        attributes.append("Expiration date=");
        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());

        JsonArray jsonArray = new JsonArray();
        JsonObject item = new JsonObject();
        item.addProperty("dsname", DATASET_NAME);
        item.addProperty("dsorg", "PO");
        jsonArray.add(item);
        jsonObject.add("items", jsonArray);
        Collection<String> memberList = new ArrayList<>();
        PowerMockito.doReturn(memberList).when(zosDatasetSpy).memberList();
        attributes = new StringBuffer();
        attributes.append("Data Set Name=");
        attributes.append(DATASET_NAME);
        attributes.append(",Volume serial=,");
        attributes.append("Organization=PO,");
        attributes.append("Record format=,");
        attributes.append("Record length=,");
        attributes.append("Block size=,");
        attributes.append("Data set name type=,");
        attributes.append("Allocated extents=,");
        attributes.append("% Utilized=,");
        attributes.append("PDS=true,");
        attributes.append("Number of members=0,");
        attributes.append("Creation date=,");
        attributes.append("Referenced date=,");
        attributes.append("Expiration date=");
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        
        Assert.assertEquals("toString() should return the valid String", attributes.toString(), zosDatasetSpy.getAttibutesAsString());        
    }
    
    @Test
    public void testGetAttibutesAsStringNotExist() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();
    
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosDatasetSpy.getAttibutesAsString();        
    }

    @Test
    public void testGetAttibutesAsStringNoRows() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("returnedRows", 0);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.getAttibutesAsString();
        
    }
    
    @Test
    public void testGetAttibutesAsStringZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
    
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosDatasetSpy.getAttibutesAsString();
    }
    
    @Test
    public void testGetAttibutesAsStringBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
    
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
    
        zosDatasetSpy.getAttibutesAsString();
    }

    @Test
    public void testGetAttibutesAsStringZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable list to attibutes of data set \"" + DATASET_NAME + "\" on image " + IMAGE);

        zosDatasetSpy.getAttibutesAsString();
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
    public void testInternalRetrieve() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(zosmfResponseMock.getContent()).thenReturn(CONTENT);
        
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));

        zosDatasetSpy.setDataType(DatasetDataType.BINARY);
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosDatasetSpy.retrieve(null));
    }
    
    @Test
    public void testInternalRetrieveZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
    
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.retrieve(null);
    }
    
    @Test
    public void testInternalRetrieveBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        
        zosDatasetSpy.retrieve(null);
    }

    @Test
    public void testInternalRetrieveBadHttpResponseException1() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve content of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.retrieve(null);
    }

    @Test
    public void testInternalRetrieveZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to retrieve content of data set \"" + DATASET_NAME + "\" on image " + IMAGE);
        
        zosDatasetSpy.retrieve(null);
    }
    
    @Test
    public void testInternalStoreText() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
        Mockito.verify(zosmfResponseMock, Mockito.times(1)).getStatusCode();
        
        Mockito.clearInvocations(zosmfResponseMock);        
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
        Mockito.verify(zosmfResponseMock, Mockito.times(2)).getStatusCode();        
        Mockito.clearInvocations(zosDatasetSpy);
    }
    
    @Test
    public void testInternalStoreTextNotExist() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreTextZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreTextBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
    }

    @Test
    public void testInternalStoreTextZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to write to data set \"" + DATASET_NAME + "(" + MEMBER_NAME + ")\" on image " + IMAGE);
        
        zosDatasetSpy.storeText(CONTENT, MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreBinary() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
        Mockito.verify(zosmfResponseMock, Mockito.times(1)).getStatusCode();
        
        Mockito.clearInvocations(zosmfResponseMock);        
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
        Mockito.verify(zosmfResponseMock, Mockito.times(2)).getStatusCode();        
        Mockito.clearInvocations(zosDatasetSpy);
    }
    
    @Test
    public void testInternalStoreBinaryNotExist() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(false).when(zosDatasetSpy).exists();

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("\"" + DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreBinaryZosmfException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
    }
    
    @Test
    public void testInternalStoreBinaryBadHttpResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("errorString");
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
    }

    @Test
    public void testInternalStoreBinaryZosmfResponseException() throws ZosDatasetException, ZosmfException {
        PowerMockito.doReturn(true).when(zosDatasetSpy).exists();
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_BINARY), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        PowerMockito.doReturn("errorString").when(zosDatasetSpy).buildErrorString(Mockito.anyString(), Mockito.any());
        
        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Unable to write to data set \"" + DATASET_NAME + "(" + MEMBER_NAME + ")\" on image " + IMAGE);
        
        zosDatasetSpy.storeBinary(CONTENT.getBytes(), MEMBER_NAME, true);
    }
    
    @Test
    public void testStoreArtifact() throws ZosFileManagerException, IOException {
        setupTestStoreArtifact();
        
        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosDatasetSpy.storeArtifact(CONTENT, "pathElement", "output.file"));
        
        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosDatasetSpy.storeArtifact(CONTENT.getBytes(), "pathElement", "output.file"));
    }
    
    @Test
    public void testStoreArtifactException1() throws ZosFileManagerException, IOException {
        setupTestStoreArtifact();
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Unable to store artifact");
        zosDatasetSpy.storeArtifact(new Object(), "pathElement", "output.file");
    }
    
    @Test
    public void testStoreArtifactException2() throws ZosFileManagerException, IOException {
        FileSystemProvider fileSystemProviderMock = setupTestStoreArtifact();
        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IOException());
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Unable to store artifact");
        zosDatasetSpy.storeArtifact(CONTENT, "pathElement", "output.file");
    }
    
    private FileSystemProvider setupTestStoreArtifact() throws IOException {
        Path archivePathMock = Mockito.mock(Path.class);
        Mockito.when(archivePathMock.toString()).thenReturn("artifactPath");
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
        ZosFileManagerImpl.setDatasetArtifactRoot(archivePathMock);
        ZosFileManagerImpl.setCurrentTestMethod("testStoreArtifact");
        
        return fileSystemProviderMock;
    }

    @Test
    public void testAddPropertyWhenSet() throws ZosDatasetException {
        JsonObject jsonObject = getJsonObject();
        JsonObject returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", null);
        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", jsonObject, returnedJsonObject);
        
        jsonObject = getJsonObject();
        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", "value");
        Assert.assertEquals("testAddPropertyWhenSet() should return the correct String property value", "value", returnedJsonObject.get("property").getAsString());

        jsonObject = getJsonObject();
        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", -1);
        Assert.assertEquals("testAddPropertyWhenSet() should return the original JsonObject", jsonObject, returnedJsonObject);
        
        jsonObject = getJsonObject();
        returnedJsonObject = zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", 99);
        Assert.assertEquals("testAddPropertyWhenSet() should return the correct int property value", 99, returnedJsonObject.get("property").getAsInt());

        exceptionRule.expect(ZosDatasetException.class);
        exceptionRule.expectMessage("Invlaid type of \"java.lang.Double\" for property \"property\" on image " + IMAGE);
        jsonObject = getJsonObject();
        zosDatasetSpy.addPropertyWhenSet(jsonObject, "property", 12.34);
    }
    
    @Test
    public void testGetMembers() {
        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("returnedRows", 0);
        Assert.assertFalse("getMembers() should return false", zosDatasetSpy.getMembers(jsonObject));
        Assert.assertEquals("datasetMembers should return a list with 0 members", listOfMembers(0), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
        
        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
        jsonObject = getJsonObject(1);
        Assert.assertFalse("getMembers() should return false", zosDatasetSpy.getMembers(jsonObject));
        Assert.assertEquals("datasetMembers should return a list with 1 member", listOfMembers(1), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
        
        Whitebox.setInternalState(zosDatasetSpy, "datasetMembers", new ArrayList<>());
        jsonObject = getJsonObject(2);
        jsonObject.addProperty("moreRows", true);
        Assert.assertTrue("getMembers() should return true", zosDatasetSpy.getMembers(jsonObject));
        Assert.assertEquals("datasetMembers should return a list with 2 members", listOfMembers(2), Whitebox.getInternalState(zosDatasetSpy,"datasetMembers"));
    }
    
    @Test
    public void testBuildErrorString() {
        String expectedString = "Error action";
        String returnString = zosDatasetSpy.buildErrorString("action", new JsonObject());
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        JsonObject jsonObject = getJsonObject();
        jsonObject.addProperty("category", 0);
        jsonObject.addProperty("rc", 0);
        jsonObject.addProperty("reason", 0);
        jsonObject.addProperty("message", "message");
        jsonObject.addProperty("id", 1);
        zosDatasetSpy.buildErrorString("action", jsonObject);
        
        jsonObject.addProperty("details", "details");
        expectedString = "Error action data set \"" + DATASET_NAME + "\", category:0, rc:0, reason:0, message:message\n" + 
                "details:details";
        returnString = zosDatasetSpy.buildErrorString("action", jsonObject);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        jsonObject.addProperty("stack", "stack");
        zosDatasetSpy.buildErrorString("action", jsonObject);
        
        jsonObject.addProperty("details", "details");
        expectedString = "Error action data set \"" + DATASET_NAME + "\", category:0, rc:0, reason:0, message:message\n" + 
                "details:details\n" + 
                "stack:\n" + 
                "stack";
        returnString = zosDatasetSpy.buildErrorString("action", jsonObject);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        jsonObject.addProperty("details", 1);
        zosDatasetSpy.buildErrorString("action", jsonObject);
        
        jsonObject.remove("details");
        JsonArray jsonArray = new JsonArray();
        JsonPrimitive item = new JsonPrimitive("details line 1");
        jsonArray.add(item);
        item = new JsonPrimitive("details line 2");
        jsonArray.add(item);
        jsonObject.add("details", jsonArray);
        expectedString = "Error action data set \"" + DATASET_NAME + "\", category:0, rc:0, reason:0, message:message\n" + 
                "details:\n" +
                "details line 1\n" +
                "details line 2\n" + 
                "stack:\n" + 
                "stack";
        returnString = zosDatasetSpy.buildErrorString("action", jsonObject);
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
    public void testGetZosmfApiProcessor() {
        Assert.assertEquals("getZosmfApiProcessor() should return the mocked IZosmfRestApiProcessor", zosmfApiProcessorMock, zosDatasetSpy.getZosmfApiProcessor());
    }
    
    private JsonObject getJsonObject() {
        return getJsonObject(1, 0);
    }

    private JsonObject getJsonObject(int count) {
        return getJsonObject(count, 0);
    }

    private JsonObject getJsonObject(int count, int startingAt) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("returnedRows", count);
        jsonObject.add("items", getJsonArray(DATASET_NAME, MEMBER_NAME, count, startingAt));
        return jsonObject;
    }

    private JsonElement getJsonArray(String datasetName, String memberName, int count, int startingAt) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < count; i++) {
            String sfx = "";
            if (startingAt == 0) {
                if (i > 0) {
                    sfx = Integer.toString(i);
                }
            } else {
                sfx = Integer.toString(i + startingAt);
            }
            JsonObject item = new JsonObject();
            item.addProperty("dsname", datasetName + sfx);
            item.addProperty("member", memberName + sfx);
            jsonArray.add(item);
        }
        return jsonArray;
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
}
