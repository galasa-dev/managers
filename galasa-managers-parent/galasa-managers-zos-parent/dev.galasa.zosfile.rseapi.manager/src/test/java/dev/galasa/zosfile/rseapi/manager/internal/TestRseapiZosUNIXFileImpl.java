/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class})
public class TestRseapiZosUNIXFileImpl {
    
    private RseapiZosUNIXFileImpl zosUNIXFile;
    
    private RseapiZosUNIXFileImpl zosUNIXFileSpy;

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
    private Log logMock;
    
    private static String logMessage;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String UNIX_DIRECTORY = "/path1/path2/path3/path4";

    private static final String UNIX_FILE = "file";

    private static final String UNIX_PATH = UNIX_DIRECTORY + "/" + UNIX_FILE;    
    
    private static final String TYPE_FILE = "FILE";
    
    private static final String TYPE_DIRECTORY = "DIRECTORY";
    
    private static final String MODE = "rwxrwxr-x";
    
    private static final String IMAGE = "IMAGE";
    
    private static final String CONTENT = "content";
    
    private static final String EXCEPTION = "exception";
    
    private static final String ERROR = "error";

    private static final String PROPERTY = "property";
    
    private static final String VALUE = "value";

    private static final int MAX_ROWS = 1000;
    
    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
        Answer<String> answer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
            	if (invocation.getArgument(0) instanceof String) {
            		logMessage = invocation.getArgument(0);
            	}
                System.err.println("Captured Log Message:\n" + logMessage);
                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
                    ((Throwable) invocation.getArgument(1)).printStackTrace();
                }
                return null;
            }
        };
        Mockito.doAnswer(answer).when(logMock).trace(Mockito.any());
        Mockito.doAnswer(answer).when(logMock).info(Mockito.any());
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        
        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
        Mockito.when(zosManagerMock.getZosFilePropertyDirectoryListMaxItems(Mockito.any())).thenReturn(MAX_ROWS);
        Mockito.when(zosManagerMock.getZosFilePropertyUnixFilePermissions(Mockito.any())).thenReturn(MODE);
        
        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
        RseapiZosFileManagerImpl.setZosManager(zosManagerMock);
        
        PowerMockito.doReturn(rseapiApiProcessorMock).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        RseapiZosFileManagerImpl.setRseapiManager(rseapiManagerMock);
        
        zosUNIXFile = new RseapiZosUNIXFileImpl(zosImageMock, UNIX_PATH);
        zosUNIXFileSpy = Mockito.spy(zosUNIXFile);
    }
    
    @Test
    public void testConstructorException1() throws RseapiManagerException, ZosFileManagerException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path must be absolute not be relative");
        new RseapiZosUNIXFileImpl(zosImageMock, "PATH");
    }
    
    @Test
    public void testConstructorException2() throws RseapiManagerException, ZosFileManagerException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        Mockito.doThrow(new RseapiManagerException(EXCEPTION)).when(rseapiManagerMock).newRseapiRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        new RseapiZosUNIXFileImpl(zosImageMock, UNIX_PATH);
    }
    
    @Test
    public void testCreate() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).createPath(Mockito.any(), Mockito.any());
       
        zosUNIXFileSpy.create();
        Assert.assertFalse("created() should return false", zosUNIXFileSpy.created());
        
        PowerMockito.doReturn(false).doReturn(true).when(zosUNIXFileSpy).exists();
        zosUNIXFileSpy.create();
        Assert.assertTrue("created() should return false", zosUNIXFileSpy.created());
        
        PowerMockito.doReturn(false).doReturn(true).when(zosUNIXFileSpy).exists();
        Whitebox.setInternalState(zosUNIXFileSpy, "retainToTestEnd", true);
        zosUNIXFileSpy.create();
        Assert.assertTrue("created() should return false", zosUNIXFileSpy.created());
        
        PowerMockito.doReturn(false).doReturn(true).when(zosUNIXFileSpy).exists();
        Whitebox.setInternalState(zosUNIXFileSpy, "fileName", (String) null);
        zosUNIXFileSpy.create();
        Assert.assertTrue("created() should return false", zosUNIXFileSpy.created());
        
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();      
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path '" + UNIX_PATH + "' already exists on image " + IMAGE);
        zosUNIXFileSpy.create();
    }
    
    @Test
    public void testCreateRetain() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(null).when(zosUNIXFileSpy).create();
       
        zosUNIXFileSpy.createRetain();
        Assert.assertTrue("createdReturn() should set retainToTestEnd to true", Whitebox.getInternalState(zosUNIXFileSpy, "retainToTestEnd"));
    }
    
    @Test
    public void testDirectoryDeleteNonEmpty() throws ZosUNIXFileException {
        PowerMockito.doNothing().when(zosUNIXFileSpy).delete(Mockito.anyString(), Mockito.anyBoolean());
       
        Assert.assertFalse("directoryDeleteNonEmpty() should return false", zosUNIXFileSpy.directoryDeleteNonEmpty());
        
    }
    
    @Test
    public void testDelete() throws ZosUNIXFileException {
        PowerMockito.doNothing().when(zosUNIXFileSpy).delete(Mockito.anyString(), Mockito.anyBoolean());
       
        Assert.assertFalse("delete() should return false", zosUNIXFileSpy.delete());
        
    }
    
    @Test
    public void testExists() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
       
        Assert.assertTrue("exists() should return true", zosUNIXFileSpy.exists());
    }
    
    @Test
    public void testStore() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        zosUNIXFileSpy.store(CONTENT);
        Assert.assertEquals("store() should log expected message", "UNIX path '" + UNIX_DIRECTORY + "' updated on image " + IMAGE, logMessage);

        zosUNIXFileSpy.setDataType(UNIXFileDataType.BINARY);
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        zosUNIXFileSpy.store(CONTENT);
        Assert.assertEquals("store() should log expected message", "UNIX path '" + UNIX_DIRECTORY + "' updated on image " + IMAGE, logMessage);
    }
    
    @Test
    public void testStoreBadHttpresponseException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Error writing to '" + UNIX_PATH + "', HTTP Status Code 404 : NOT_FOUND");
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreNotExistException() throws ZosUNIXFileException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE);
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreIsDirectoryException() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, '" + UNIX_PATH + "' is a directory");
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreRseapiException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());

        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());

        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testRetrieve() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        PowerMockito.doReturn(CONTENT).when(zosUNIXFileSpy).retrieve(Mockito.any());
        Assert.assertEquals("retrieve() should return the supplied value", CONTENT, zosUNIXFileSpy.retrieve());
    }
    
    @Test
    public void testRetrieveException1() throws ZosUNIXFileException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE);        
        zosUNIXFileSpy.retrieve();
    }
    
    @Test
    public void testRetrieveException2() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, '" + UNIX_PATH + "' is a directory");        
        zosUNIXFileSpy.retrieve();
    }
    
    @Test
    public void testOneLineMethods() throws ZosUNIXFileException {
        PowerMockito.doNothing().when(zosUNIXFileSpy).saveToResultsArchive(Mockito.any());
        zosUNIXFileSpy.saveToResultsArchive();

        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        zosUNIXFileSpy.isDirectory();

        Map<String, String> dirList = new HashMap<>();
        dirList.put(UNIX_PATH, MODE);
        PowerMockito.doReturn(dirList).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
        Assert.assertTrue("directoryList() should return expected content", dirList.equals(zosUNIXFileSpy.directoryList()));

        Assert.assertTrue("directoryListRecursive() should return expected content", dirList.equals(zosUNIXFileSpy.directoryListRecursive()));

        Assert.assertEquals("getUnixPath() should return the expected value", UNIX_PATH, zosUNIXFileSpy.getUnixPath());

        Assert.assertEquals("getFileName() should return the expected value", UNIX_FILE, zosUNIXFileSpy.getFileName());

        Assert.assertEquals("getDirectoryPath() should return the expected value", UNIX_DIRECTORY, zosUNIXFileSpy.getDirectoryPath());
        
        PowerMockito.doReturn(MODE).when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        Assert.assertEquals("getAttributesAsString() should return the expected value", MODE, zosUNIXFileSpy.getAttributesAsString());

        Assert.assertEquals("quoted() should return the expected value", "'" + UNIX_PATH + "'", zosUNIXFileSpy.quoted(UNIX_PATH));

        Assert.assertEquals("logOnImage() should return the expected value", " on image " + IMAGE , zosUNIXFileSpy.logOnImage());

        Assert.assertEquals("toString() should return the expected value", UNIX_PATH, zosUNIXFileSpy.toString());
        
        Assert.assertFalse("retainToTestEnd() should return false", zosUNIXFileSpy.retainToTestEnd());
        
        Assert.assertFalse("deleted() should return false", zosUNIXFileSpy.deleted());
    }
    
    @Test
    public void testDataType() {
        zosUNIXFileSpy.setDataType(UNIXFileDataType.TEXT);
        Assert.assertEquals("getDataType() should return the expected value", UNIXFileDataType.TEXT, zosUNIXFileSpy.getDataType());
        zosUNIXFileSpy.setDataType(UNIXFileDataType.BINARY);
        Assert.assertEquals("getDataType() should return the expected value", UNIXFileDataType.BINARY, zosUNIXFileSpy.getDataType());
    }
    
    @Test
    public void testGetAttributesAsString() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", "name");
        requestBody.addProperty("type", TYPE_FILE);    
        requestBody.addProperty("permissionsSymbolic", "permissionsSymbolic");    
        requestBody.addProperty("size", 0);    
        requestBody.addProperty("fileOwner", "fileOwner");     
        requestBody.addProperty("group", "group");    
        requestBody.addProperty("lastModified", "lastModified");     
        requestBody.addProperty("encoding", "encoding");

        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(requestBody);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Answer<?> emptyStringWhenNullValue = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
            	if (invocation.getArgument(1).equals("type")) {
            		return (String) invocation.callRealMethod();
            	}
                return invocation.getArgument(1);
            }
        };
        PowerMockito.doReturn(TYPE_FILE).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.eq(TYPE_FILE));
        PowerMockito.doReturn(TYPE_DIRECTORY).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.eq(TYPE_DIRECTORY));
        PowerMockito.doAnswer(emptyStringWhenNullValue).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.any());
        String returnedValue = "Name=" + UNIX_FILE + ",Type=" + TYPE_FILE + ",Mode=permissionsSymbolic,Size=size,User=fileOwner,Group=group,Modified=lastModified,Encoding=encoding";
        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_FILE));

        requestBody.addProperty("type", TYPE_DIRECTORY);
        returnedValue = "Name=" + UNIX_PATH + ",Type=" + TYPE_DIRECTORY + ",IsEmpty=true,Mode=permissionsSymbolic,Size=size,User=fileOwner,Group=group,Modified=lastModified,Encoding=encoding";
        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_PATH + "/"));

        JsonArray children = new JsonArray();
        requestBody.add("children", children);
        returnedValue = "Name=" + UNIX_PATH + ",Type=" + TYPE_DIRECTORY + ",IsEmpty=false,Mode=permissionsSymbolic,Size=size,User=fileOwner,Group=group,Modified=lastModified,Encoding=encoding";
        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_PATH + "/"));
    }
    
    @Test
    public void testGetAttributesAsStringNotExistException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testGetAttributesAsStringRseapiException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testGetAttributesAsStringRseapiResponseException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);

        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to list UNIX path '" + UNIX_PATH + "' on image " + IMAGE);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testGetAttributesAsStringBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testCreatePath() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
        
        Assert.assertTrue("createPath() should return true", zosUNIXFileSpy.createPath(UNIX_PATH, TYPE_FILE));
      
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosUNIXFileSpy.createPath(UNIX_PATH, TYPE_FILE);
    }
    
    @Test
    public void testCreatePathBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        zosUNIXFileSpy.createPath(UNIX_PATH, TYPE_FILE);
    }
    
    @Test
    public void testInternalDelete() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn("Type=" + TYPE_FILE).when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);        
        zosUNIXFileSpy.delete(UNIX_FILE, false);

        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn("Type=" + TYPE_DIRECTORY + "IsEmpty=true").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        zosUNIXFileSpy.delete(UNIX_DIRECTORY, true);
        Assert.assertTrue("delete() should set deleted to true", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
        
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        zosUNIXFileSpy.delete(UNIX_FILE, false);
        Assert.assertFalse("delete() should set deleted to false", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
    }
    
    @Test
    public void testInternalDeletenotExistException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE);
        zosUNIXFileSpy.delete(UNIX_PATH, false);
    }
    
    @Test
    public void testInternalDeleteNotDirectory() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, UNIX path '" + UNIX_FILE + "' is not a directory");
        zosUNIXFileSpy.delete(UNIX_FILE, true);
    }
    
    @Test
    public void testInternalDeleteNotEmptyDirectory() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn("Type=DIRECTORY,IsEmpty=false").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, UNIX path '" + UNIX_FILE + "' is a directory and is not empty. Use the directoryDeleteNonEmpty() method");
        zosUNIXFileSpy.delete(UNIX_FILE, false);
    }
    
    @Test
    public void testInternalDeleteRseapiException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        zosUNIXFileSpy.delete(UNIX_FILE, false);
    }
    
    @Test
    public void testInternalDeleteBadHttpresponseException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.delete(UNIX_FILE, false);
    }
    
    @Test
    public void testInternalExists() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Assert.assertTrue("exists() should set deleted to true", zosUNIXFileSpy.exists(UNIX_DIRECTORY));

        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Assert.assertFalse("exists() should set deleted to false", zosUNIXFileSpy.exists(UNIX_DIRECTORY + "/"));
    }
    
    @Test
    public void testInternalRseapiExistsException() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosUNIXFileSpy.exists(UNIX_PATH);
    }
    
    @Test
    public void testInternalExistsException() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_ACCEPTABLE);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.exists(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieve() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(UNIXFileDataType.TEXT).when(zosUNIXFileSpy).getDataType();
        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject responseBody = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(responseBody);
        Assert.assertEquals("retrieve() should return the expected value", "", zosUNIXFileSpy.retrieve(UNIX_PATH));
        
        responseBody.addProperty(CONTENT, CONTENT);
        Assert.assertEquals("retrieve() should return the expected value", CONTENT, zosUNIXFileSpy.retrieve(UNIX_PATH));
        
        PowerMockito.doReturn(UNIXFileDataType.BINARY).when(zosUNIXFileSpy).getDataType();
        Assert.assertEquals("retrieve() should return the expected value", CONTENT, zosUNIXFileSpy.retrieve(UNIX_PATH));
    }
    
    @Test
    public void testInternalRetrieveRseapiException() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieveGetContentException() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to retrieve content of '" + UNIX_PATH + "' on image " + IMAGE);
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieveRseapiBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieveGetJsonContentException() throws ZosUNIXFileException, RseapiException {
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");

        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Error retrieve content '" + UNIX_PATH + "', HTTP Status Code 404 : NOT_FOUND");
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testSaveToResultsArchive() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        PowerMockito.doReturn(CONTENT).when(zosUNIXFileSpy).retrieve(Mockito.any());
        PowerMockito.doReturn("location").when(zosUNIXFileSpy).storeArtifact(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        zosUNIXFileSpy.saveToResultsArchive(UNIX_PATH);
        Assert.assertEquals("saveToResultsArchive() should log expected message", "'" + UNIX_PATH + "' archived to location", logMessage);
        

        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        SortedMap<String, String> paths = new TreeMap<>();
        paths.put("directory", TYPE_DIRECTORY);
        paths.put("file", TYPE_FILE);
        paths.put("unknown", "unknown");
        PowerMockito.doReturn(paths).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
        zosUNIXFileSpy.saveToResultsArchive(UNIX_PATH);

        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path '" + UNIX_PATH + "' does not exist on image " + IMAGE);
        zosUNIXFileSpy.saveToResultsArchive(UNIX_PATH);
    }
    
    @Test
    public void testIsDirectory() throws ZosUNIXFileException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));

        PowerMockito.doReturn("Type=FILE").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_DIRECTORY));

        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));

        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));
    }
    
    @Test
    public void testListDirectory() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject );
        Map<String, String> paths = new TreeMap<>();
        paths.put("path1", TYPE_DIRECTORY);
        PowerMockito.doReturn(paths).when(zosUNIXFileSpy).getPaths(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        
        Assert.assertTrue("listDirectory() should return expected content", paths.equals(zosUNIXFileSpy.listDirectory("path1/", false)));
        
        
        paths.put("path2", TYPE_DIRECTORY);
        paths.put("path3", TYPE_DIRECTORY);
        paths.put("path4", TYPE_DIRECTORY);
        
        Assert.assertTrue("listDirectory() should return expected content", paths.equals(zosUNIXFileSpy.listDirectory(UNIX_DIRECTORY, true)));
    }
    
    @Test
    public void testListDirectoryNotDirectory() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, 'path1' is not a directory");

        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testListDirectoryRseapiException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testListDirectoryRseapiResponseException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to list UNIX path 'path1' on image " + IMAGE);

        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testListDirectoryBadHttpResponseException() throws ZosUNIXFileException, RseapiException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.eq(RseapiRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testGetPaths() throws ZosUNIXFileException {
    	JsonObject requestBody = new JsonObject();
        Map<String, String> expectedResult = new TreeMap<>();        
        Assert.assertEquals("getPaths() should return expected content", expectedResult, zosUNIXFileSpy.getPaths("/root", requestBody, false));
        
        JsonArray children = new JsonArray();
        JsonObject fileChild = new JsonObject();
        fileChild.addProperty("name", "file");
        fileChild.addProperty("type", TYPE_FILE);
        children.add(fileChild);
        requestBody.add("children", children);
        expectedResult.put("/root/file", TYPE_FILE);        
        Map<String, String> result = zosUNIXFileSpy.getPaths("/root/", requestBody, false);
        Assert.assertEquals("getPaths() should return expected content", expectedResult, result);

        JsonObject fileDirectory = new JsonObject();
        fileDirectory.addProperty("name", "directory");
        fileDirectory.addProperty("type", TYPE_DIRECTORY);
        children.add(fileDirectory);
        expectedResult.put("/root/directory", TYPE_DIRECTORY);
        PowerMockito.doReturn(new TreeMap<>()).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
        result = zosUNIXFileSpy.getPaths("/root/", requestBody, false);
        Assert.assertEquals("getPaths() should return expected content", expectedResult, result);

        result = zosUNIXFileSpy.getPaths("/root/", requestBody, true);
        Assert.assertEquals("getPaths() should return expected content", expectedResult, result);
    }
    
    @Test
    public void testStoreArtifact() throws ZosFileManagerException, IOException {
        setupTestStoreArtifact();
        
        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosUNIXFileSpy.storeArtifact(CONTENT, true, "pathElement"));
        
        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosUNIXFileSpy.storeArtifact(CONTENT, false, "pathElement", "output.file"));
        
        Assert.assertEquals("storeArtifact() should return the supplied mock value", "artifactPath", zosUNIXFileSpy.storeArtifact(CONTENT.getBytes(), false, "pathElement", "output.file"));
    }
    
    @Test
    public void testStoreArtifactException1() throws ZosFileManagerException, IOException {
        setupTestStoreArtifact();
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Unable to store artifact");
        zosUNIXFileSpy.storeArtifact(new Object(), false, "pathElement", "output.file");
    }
    
    @Test
    public void testStoreArtifactException2() throws ZosFileManagerException, IOException {
        FileSystemProvider fileSystemProviderMock = setupTestStoreArtifact();
        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IOException());
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Unable to store artifact");
        zosUNIXFileSpy.storeArtifact(CONTENT, false, "pathElement", "output.file");
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
        RseapiZosFileManagerImpl.setUnixPathArtifactRoot(archivePathMock);
        RseapiZosFileManagerImpl.setCurrentTestMethodArchiveFolderName("testStoreArtifact");
        
        return fileSystemProviderMock;
    }
    @Test
    public void testSplitUnixPath() {
        Whitebox.setInternalState(zosUNIXFileSpy, "unixPath", UNIX_PATH);
        Whitebox.setInternalState(zosUNIXFileSpy, "directoryPath", (String) null);
        Whitebox.setInternalState(zosUNIXFileSpy, "type", (String) null);
        
        zosUNIXFileSpy.splitUnixPath();
        Assert.assertEquals("splitUnixPath() should set the expected value", UNIX_FILE, Whitebox.getInternalState(zosUNIXFileSpy, "fileName"));
        Assert.assertEquals("splitUnixPath() should set the expected value", UNIX_DIRECTORY, Whitebox.getInternalState(zosUNIXFileSpy, "directoryPath"));
        Assert.assertEquals("splitUnixPath() should set the expected value", TYPE_FILE, Whitebox.getInternalState(zosUNIXFileSpy, "type"));

        Whitebox.setInternalState(zosUNIXFileSpy, "unixPath", UNIX_DIRECTORY + "/");
        Whitebox.setInternalState(zosUNIXFileSpy, "directoryPath", (String) null);
        Whitebox.setInternalState(zosUNIXFileSpy, "type", (String) null);
        
        zosUNIXFileSpy.splitUnixPath();
        Assert.assertEquals("splitUnixPath() should set the expected value", (String) null, Whitebox.getInternalState(zosUNIXFileSpy, "fileName"));
        Assert.assertEquals("splitUnixPath() should set the expected value", UNIX_DIRECTORY, Whitebox.getInternalState(zosUNIXFileSpy, "directoryPath"));
        Assert.assertEquals("splitUnixPath() should set the expected value", TYPE_DIRECTORY, Whitebox.getInternalState(zosUNIXFileSpy, "type"));
    }
    
    @Test
    public void testEmptyStringWhenNull() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PROPERTY, VALUE);
        
        Assert.assertEquals("emptyStringWhenNull() should return the expected value", "", zosUNIXFileSpy.emptyStringWhenNull(jsonObject, "xxxx"));
        Assert.assertEquals("emptyStringWhenNull() should return the expected value", VALUE, zosUNIXFileSpy.emptyStringWhenNull(jsonObject, PROPERTY));
    }
    
    @Test
    public void testBuildErrorString() throws RseapiException {
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("OK");
        Mockito.when(rseapiResponseMock.getContent()).thenReturn(null);
        String expectedString = "Error action, HTTP Status Code " + HttpStatus.SC_OK + " : OK";
        String returnString = zosUNIXFileSpy.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        Mockito.when(rseapiResponseMock.getContent()).thenReturn(0);
        returnString = zosUNIXFileSpy.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", "status");
        jsonObject.addProperty("message", "message");
        Mockito.when(rseapiResponseMock.getContent()).thenReturn(jsonObject);
        expectedString = "Error action, HTTP Status Code " + HttpStatus.SC_OK + " : OK\n" + 
        		"status: status\n" + 
        		"message: message";
        returnString = zosUNIXFileSpy.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);

        Mockito.when(rseapiResponseMock.getContent()).thenReturn("STRING");
        expectedString = "Error action, HTTP Status Code " + HttpStatus.SC_OK + " : OK response body:\n" + 
        		"STRING";
        returnString = zosUNIXFileSpy.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        Mockito.when(rseapiResponseMock.getContent()).thenThrow(new RseapiException());
        expectedString = "Error action, HTTP Status Code " + HttpStatus.SC_OK + " : OK";
        returnString = zosUNIXFileSpy.buildErrorString("action", rseapiResponseMock);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
    }
    
    @Test
    public void testCleanCreatedPath() throws ZosUNIXFileException {
        zosUNIXFileSpy.cleanCreatedPath();        
        Whitebox.setInternalState(zosUNIXFileSpy, "createdPath", UNIX_PATH);
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());

        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        zosUNIXFileSpy.cleanCreatedPath();
        
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doNothing().when(zosUNIXFileSpy).cleanCreatedPathStore();
        PowerMockito.doNothing().when(zosUNIXFileSpy).cleanCreatedDelete();
        zosUNIXFileSpy.cleanCreatedPath();

        Mockito.doThrow(new ZosUNIXFileException(EXCEPTION)).when(zosUNIXFileSpy).exists(Mockito.any());
        zosUNIXFileSpy.cleanCreatedPath();
        
        Mockito.verify(zosUNIXFileSpy, Mockito.times(4)).cleanCreatedPath();
    }
    
    @Test
    public void testCleanCreatedPathStore() throws ZosUNIXFileException {
        PowerMockito.doNothing().when(zosUNIXFileSpy).saveToResultsArchive(Mockito.any());
        zosUNIXFileSpy.cleanCreatedPathStore();
        
        PowerMockito.doThrow(new ZosUNIXFileException(EXCEPTION)).when(zosUNIXFileSpy).saveToResultsArchive(Mockito.any());
        zosUNIXFileSpy.cleanCreatedPathStore();
        
        Mockito.verify(zosUNIXFileSpy, Mockito.times(2)).cleanCreatedPathStore();
    }
    
    @Test
    public void testCleanCreatedDelete() throws ZosUNIXFileException {
        PowerMockito.doNothing().when(zosUNIXFileSpy).delete(Mockito.any(), Mockito.anyBoolean());
        zosUNIXFileSpy.cleanCreatedDelete();
        
        PowerMockito.doThrow(new ZosUNIXFileException(EXCEPTION)).when(zosUNIXFileSpy).delete(Mockito.any(), Mockito.anyBoolean());
        zosUNIXFileSpy.cleanCreatedDelete();
        
        Mockito.verify(zosUNIXFileSpy, Mockito.times(2)).cleanCreatedDelete();
    }
}
