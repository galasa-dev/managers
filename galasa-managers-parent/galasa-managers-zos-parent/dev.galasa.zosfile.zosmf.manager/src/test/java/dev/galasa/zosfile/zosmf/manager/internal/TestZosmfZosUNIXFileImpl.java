/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

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
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
public class TestZosmfZosUNIXFileImpl {
    
    private ZosmfZosUNIXFileImpl zosUNIXFile;
    
    private ZosmfZosUNIXFileImpl zosUNIXFileSpy;

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private ZosManagerImpl zosManagerMock;
    
    @Mock
    private ZosmfManagerImpl zosmfManagerMock;
    
    @Mock
    private IZosmfRestApiProcessor zosmfApiProcessorMock;
    
    @Mock
    private IZosmfResponse zosmfResponseMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String UNIX_DIRECTORY = "/path1/path2/path3/path4";

    private static final String UNIX_FILE = "file";

    private static final String UNIX_PATH = UNIX_DIRECTORY + "/" + UNIX_FILE;    
    
    private static final String TYPE_FILE = "file";
    
    private static final String TYPE_DIRECTORY = "directory";
    
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
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        
        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
        Mockito.when(zosManagerMock.getZosFilePropertyDirectoryListMaxItems(Mockito.any())).thenReturn(MAX_ROWS);
        Mockito.when(zosManagerMock.getZosFilePropertyUnixFilePermissions(Mockito.any())).thenReturn(MODE);
        
        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
        ZosmfZosFileManagerImpl.setZosManager(zosManagerMock);
        
        PowerMockito.doReturn(zosmfApiProcessorMock).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        ZosmfZosFileManagerImpl.setZosmfManager(zosmfManagerMock);
        
        zosUNIXFile = new ZosmfZosUNIXFileImpl(zosImageMock, UNIX_PATH);
        zosUNIXFileSpy = Mockito.spy(zosUNIXFile);
    }
    
    @Test
    public void testConstructorException1() throws ZosmfManagerException, ZosFileManagerException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path must be absolute not be relative");
        new ZosmfZosUNIXFileImpl(zosImageMock, "PATH");
    }
    
    @Test
    public void testConstructorException2() throws ZosmfManagerException, ZosFileManagerException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        Mockito.doThrow(new ZosmfManagerException(EXCEPTION)).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
        new ZosmfZosUNIXFileImpl(zosImageMock, UNIX_PATH);
    }
    
    @Test
    public void testConstructorException3() throws ZosmfManagerException, ZosFileManagerException {
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        Mockito.when(zosManagerMock.getZosFilePropertyDirectoryListMaxItems(Mockito.any())).thenThrow(new ZosFileManagerException(EXCEPTION));
        new ZosmfZosUNIXFileImpl(zosImageMock, UNIX_PATH);
    }
    
    @Test
    public void testCreate() throws ZosUNIXFileException, ZosmfException {
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
        exceptionRule.expectMessage("UNIX path \"" + UNIX_PATH + "\" already exists on image " + IMAGE);
        zosUNIXFileSpy.create();
    }
    
    @Test
    public void testCreateRetain() throws ZosUNIXFileException, ZosmfException {
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
    public void testStore() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());

        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
       
        zosUNIXFileSpy.store(CONTENT);
        
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
        zosUNIXFileSpy.store(CONTENT);

        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists();
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path \"" + UNIX_PATH + "\" does not exist on image " + IMAGE);
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreIsDirectoryException() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, \"" + UNIX_PATH + "\" is a directory");
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreZosmfException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());

        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreBadHttpResponseException1() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());

        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
       
        zosUNIXFileSpy.store(CONTENT);
    }
    
    @Test
    public void testStoreBadHttpResponseException2() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory();
        PowerMockito.doReturn(true).doReturn(false).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());

        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_TEXT), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to write to UNIX path \"" + UNIX_PATH + "\" on image " + IMAGE);
       
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
        exceptionRule.expectMessage("UNIX path \"" + UNIX_PATH + "\" does not exist on image " + IMAGE);
        
        zosUNIXFileSpy.retrieve();
    }
    
    @Test
    public void testRetrieveException2() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory();
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, \"" + UNIX_PATH + "\" is a directory");
        
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

        Assert.assertEquals("quoted() should return the expected value", "\"" + UNIX_PATH + "\"", zosUNIXFileSpy.quoted(UNIX_PATH));

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
    public void testGetAttributesAsString() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        JsonObject items = new JsonObject();
        items.addProperty("name", UNIX_FILE);
        items.addProperty("type", "type");    
        items.addProperty("mode", "mode");    
        items.addProperty("size", "size");    
        items.addProperty("uid", "uid");     
        items.addProperty("user", "user");    
        items.addProperty("gid", "gid");     
        items.addProperty("group", "group");   
        items.addProperty("modified", "modified");
        items.addProperty("target", "target");  
        jsonArray.add(items);
        jsonObject.add("items", jsonArray);

        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        PowerMockito.doReturn("file").when(zosUNIXFileSpy).determineType(Mockito.any());
        Answer<?> emptyStringWhenNullValue = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1);
            }
        };
        PowerMockito.doAnswer(emptyStringWhenNullValue).when(zosUNIXFileSpy).emptyStringWhenNull(Mockito.any(), Mockito.any());
        String returnedValue = "Name=name,Type=file,Mode=mode,Size=size,UID=uid,User=user,GID=gid,Group=group,Modified=mtime,Target=target";
        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_PATH));
        
        Assert.assertEquals("getAttributesAsString() should return the expected value", returnedValue, zosUNIXFileSpy.getAttributesAsString(UNIX_DIRECTORY + "/"));
    }
    
    @Test
    public void testGetAttributesAsStringNotExistException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path \"" + UNIX_PATH + "\" does not exist on image " + IMAGE);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testGetAttributesAsStringZosmfException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testGetAttributesAsStringZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);

        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to list UNIX path \"" + UNIX_PATH + "\" on image " + IMAGE);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testGetAttributesAsStringBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        zosUNIXFileSpy.getAttributesAsString(UNIX_PATH);
    }
    
    @Test
    public void testDetermineType() {
        Assert.assertEquals("determineType() should return the expected value", "file", zosUNIXFileSpy.determineType("-"));
        Assert.assertEquals("determineType() should return the expected value", "character", zosUNIXFileSpy.determineType("c"));
        Assert.assertEquals("determineType() should return the expected value", "directory", zosUNIXFileSpy.determineType("d"));
        Assert.assertEquals("determineType() should return the expected value", "extlink", zosUNIXFileSpy.determineType("e"));
        Assert.assertEquals("determineType() should return the expected value", "symblink", zosUNIXFileSpy.determineType("l"));
        Assert.assertEquals("determineType() should return the expected value", "FIFO", zosUNIXFileSpy.determineType("p"));
        Assert.assertEquals("determineType() should return the expected value", "socket", zosUNIXFileSpy.determineType("s"));
        Assert.assertEquals("determineType() should return the expected value", "UNKNOWN", zosUNIXFileSpy.determineType("?"));
    }
    
    @Test
    public void testCreatePath() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
        
        Assert.assertTrue("createPath() should return true", zosUNIXFileSpy.createPath(UNIX_PATH, TYPE_FILE));
      
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosUNIXFileSpy.createPath(UNIX_PATH, TYPE_FILE);
    }
    
    @Test
    public void testCreatePathBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        zosUNIXFileSpy.createPath(UNIX_PATH, TYPE_FILE);
    }
    
    @Test
    public void testCreatePathZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.POST_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to create UNIX path \"" + UNIX_PATH + "\" on image " + IMAGE);
        zosUNIXFileSpy.createPath(UNIX_PATH, TYPE_FILE);
    }
    
    @Test
    public void testInternalDelete() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);
        
        zosUNIXFileSpy.delete(UNIX_PATH, false);
        Assert.assertTrue("delete() should set deleted to true", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
        
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        zosUNIXFileSpy.delete(UNIX_PATH, true);
        Assert.assertFalse("delete() should set deleted to false", Whitebox.getInternalState(zosUNIXFileSpy, "deleted"));
    }
    
    @Test
    public void testInternalDeletenotExistException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path \"" + UNIX_PATH + "\" does not exist on image " + IMAGE);
        zosUNIXFileSpy.delete(UNIX_PATH, false);
    }
    
    @Test
    public void testInternalDeleteNotDirectory() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, UNIX path \"" + UNIX_PATH + "\" is not a directory");
        zosUNIXFileSpy.delete(UNIX_PATH, true);
    }
    
    @Test
    public void testInternalDeleteZosmfException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        zosUNIXFileSpy.delete(UNIX_PATH, false);
    }
    
    @Test
    public void testInternalDeleteBadHttpresponseException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.delete(UNIX_PATH, false);
    }
    
    @Test
    public void testInternalDeleteZosmfResposeException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.DELETE), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to delete UNIX path \"" + UNIX_PATH + "\" on image " + IMAGE);
        zosUNIXFileSpy.delete(UNIX_PATH, false);
    }
    
    @Test
    public void testInternalExists() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Assert.assertTrue("exists() should set deleted to true", zosUNIXFileSpy.exists(UNIX_DIRECTORY));

        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Assert.assertFalse("exists() should set deleted to false", zosUNIXFileSpy.exists(UNIX_DIRECTORY + "/"));
    }
    
    @Test
    public void testInternalZosmfExistsException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosUNIXFileSpy.exists(UNIX_PATH);
    }
    
    @Test
    public void testInternalExistsZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to list UNIX path \"" + UNIX_PATH + "\" on image " + IMAGE);
        
        zosUNIXFileSpy.exists(UNIX_PATH);
    }
    
    @Test
    public void testInternalExistsException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_ACCEPTABLE);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.exists(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieve() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).created();
        PowerMockito.doReturn(true).doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(zosmfResponseMock.getTextContent()).thenReturn(CONTENT);
        
        Assert.assertEquals("retrieve() should return the expected value", CONTENT, zosUNIXFileSpy.retrieve(UNIX_PATH));
    }
    
    @Test
    public void testInternalRetrieveZosmfException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieveGetTextContentException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        Mockito.when(zosmfResponseMock.getTextContent()).thenThrow(new ZosmfException(EXCEPTION));
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to retrieve content of \"" + UNIX_PATH + "\" on image " + IMAGE);
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieveZosmfBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testInternalRetrieveGetJsonContentException() throws ZosUNIXFileException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);

        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to retrieve content of \"" + UNIX_PATH + "\" on image " + IMAGE);
        
        zosUNIXFileSpy.retrieve(UNIX_PATH);
    }
    
    @Test
    public void testSaveToResultsArchive() throws ZosUNIXFileException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        PowerMockito.doReturn(CONTENT).when(zosUNIXFileSpy).retrieve(Mockito.any());
        PowerMockito.doReturn("location").when(zosUNIXFileSpy).storeArtifact(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        zosUNIXFileSpy.saveToResultsArchive(UNIX_PATH);

        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        SortedMap<String, String> paths = new TreeMap<>();
        paths.put("directory", TYPE_DIRECTORY);
        paths.put("file", TYPE_FILE);
        paths.put("unknown", "unknown");
        PowerMockito.doReturn(paths).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
        zosUNIXFileSpy.saveToResultsArchive(UNIX_PATH);

        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("UNIX path \"" + UNIX_PATH + "\" does not exist on image " + IMAGE);
        zosUNIXFileSpy.saveToResultsArchive(UNIX_PATH);
    }
    
    @Test
    public void testIsDirectory() throws ZosUNIXFileException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));

        PowerMockito.doReturn("Type=file").when(zosUNIXFileSpy).getAttributesAsString(Mockito.any());
        
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_DIRECTORY));

        PowerMockito.doReturn(true).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));

        PowerMockito.doReturn(false).when(zosUNIXFileSpy).exists(Mockito.any());
        Assert.assertFalse("listDirectory() should return false", zosUNIXFileSpy.isDirectory(UNIX_PATH));
    }
    
    @Test
    public void testListDirectory() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject );
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
    public void testListDirectoryNotDirectory() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(false).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Invalid request, \"path1\" is not a directory");

        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testListDirectoryZosmfException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(EXCEPTION);

        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testListDirectoryZosmfResponseException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException(EXCEPTION));
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("Unable to list UNIX path \"path1\" on image " + IMAGE);

        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testListDirectoryBadHttpResponseException() throws ZosUNIXFileException, ZosmfException {
        PowerMockito.doReturn(true).when(zosUNIXFileSpy).isDirectory(Mockito.any());
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        JsonObject jsonObject = new JsonObject();
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject);
        PowerMockito.doReturn(ERROR).when(zosUNIXFileSpy).buildErrorString(Mockito.any(), Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage(ERROR);
        
        zosUNIXFileSpy.listDirectory("path1", false);
    }
    
    @Test
    public void testGetPaths() throws ZosUNIXFileException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("returnedRows", 0);
        jsonObject.addProperty("totalRows", 0);    
        Map<String, String> paths = new TreeMap<>();    
        Map<String, String> result = new TreeMap<>();
        PowerMockito.doReturn(paths).when(zosUNIXFileSpy).listDirectory(Mockito.any(), Mockito.anyBoolean());
        
        Assert.assertTrue("getPaths() should return expected content", result.equals(zosUNIXFileSpy.getPaths("/root", jsonObject, false)));
        
        JsonArray jsonArray = new JsonArray();
        JsonObject items = new JsonObject();
        items.addProperty("name", ".");   
        items.addProperty("mode", "drwxrwxrwx");  
        jsonArray.add(items);
        items = new JsonObject();
        items.addProperty("name", "..");   
        items.addProperty("mode", "drwxrwxrwx");  
        jsonArray.add(items);
        items = new JsonObject();
        items.addProperty("name", "path1");   
        items.addProperty("mode", "drwxrwxrwx");  
        jsonArray.add(items);
        items = new JsonObject();
        items.addProperty("name", "file1");   
        items.addProperty("mode", "-rwxrwxrwx");  
        jsonArray.add(items);
        jsonObject.add("items", jsonArray);
        jsonObject.addProperty("returnedRows", 4);
        jsonObject.addProperty("totalRows", 4);   
        result.put("/root/path1", TYPE_DIRECTORY);  
        result.put("/root/file1", TYPE_FILE);
        
        Assert.assertTrue("getPaths() should return expected content", result.equals(zosUNIXFileSpy.getPaths("/root/", jsonObject, false)));
        
        jsonObject.addProperty("returnedRows", MAX_ROWS);
        jsonObject.addProperty("totalRows", 9999);
        exceptionRule.expect(ZosUNIXFileException.class);
        exceptionRule.expectMessage("The number of files and directories (9999) in UNIX path \"/root/\" is greater than the maximum allowed rows (" + MAX_ROWS + ")");
        
        zosUNIXFileSpy.getPaths("/root/", jsonObject, true);
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
        ZosmfZosFileManagerImpl.setUnixPathArtifactRoot(archivePathMock);
        ZosmfZosFileManagerImpl.setCurrentTestMethodArchiveFolderName("testStoreArtifact");
        
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
    public void testBuildErrorString() {
        String expectedString = "Error action";
        String returnString = zosUNIXFileSpy.buildErrorString("action", new JsonObject(), UNIX_PATH);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("returnedRows", 1);
        JsonArray jsonArray = new JsonArray();
        JsonObject items = new JsonObject();
        items.addProperty("path", UNIX_PATH);
        jsonArray.add(items);
        jsonObject.add("items", jsonArray);
        jsonObject.addProperty("category", 0);
        jsonObject.addProperty("rc", 0);
        jsonObject.addProperty("reason", 0);
        jsonObject.addProperty("message", "message");
        jsonObject.addProperty("id", 1);
        zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
        
        jsonObject.addProperty("details", "details");
        expectedString = "Error action UNIX path \"" + UNIX_PATH + "\", category:0, rc:0, reason:0, message:message\n" + 
                "details:details";
        returnString = zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        jsonObject.addProperty("stack", "stack");
        zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
        
        jsonObject.addProperty("details", "details");
        expectedString = "Error action UNIX path \"" + UNIX_PATH + "\", category:0, rc:0, reason:0, message:message\n" + 
                "details:details\n" + 
                "stack:\n" + 
                "stack";
        returnString = zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
        Assert.assertEquals("buildErrorString() should return the valid String", returnString, expectedString);
        
        jsonObject.addProperty("details", 1);
        zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
        
        jsonObject.remove("details");
        jsonArray = new JsonArray();
        JsonPrimitive item = new JsonPrimitive("details line 1");
        jsonArray.add(item);
        item = new JsonPrimitive("details line 2");
        jsonArray.add(item);
        jsonObject.add("details", jsonArray);
        expectedString = "Error action UNIX path \"" + UNIX_PATH + "\", category:0, rc:0, reason:0, message:message\n" + 
                "details:\n" +
                "details line 1\n" +
                "details line 2\n" + 
                "stack:\n" + 
                "stack";
        returnString = zosUNIXFileSpy.buildErrorString("action", jsonObject, UNIX_PATH);
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
