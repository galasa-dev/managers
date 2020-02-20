/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

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

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.zosmf.manager.internal.properties.RestrictToImage;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestrictToImage.class})
public class TestZosConsoleCommandImpl {
    
    private ZosConsoleCommandImpl zosConsoleCommand;
    
    private ZosConsoleCommandImpl zosConsoleCommandSpy;

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

    private static final String CONSOLE_COMMAND = "ZOS CONSOLE_COMMAND";

    private static final String CONSOLE_NAME = "CNAME";

    private static final String CONSOLE_RESOPNSE = "ZOS CONSOLE_RESPONSE";
    
    @Before
    public void setup() throws Exception {
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        PowerMockito.mockStatic(RestrictToImage.class);
        Mockito.when(RestrictToImage.get(Mockito.any())).thenReturn(true);

        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictToImage.get(zosImageMock.getImageID()))).thenReturn(zosmfApiProcessorMock);
        ZosConsoleManagerImpl.setZosmfManager(zosmfManagerMock);
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosConsoleCommand = new ZosConsoleCommandImpl(CONSOLE_COMMAND, CONSOLE_NAME, zosImageMock);
        zosConsoleCommandSpy = Mockito.spy(zosConsoleCommand);
    }
    
    @Test
    public void testConstructorException() throws ZosmfManagerException, ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictToImage.get(zosImageMock.getImageID()))).thenThrow(new ZosmfManagerException("exception"));
        new ZosConsoleCommandImpl(CONSOLE_COMMAND, CONSOLE_NAME, zosImageMock);
    }
    
    @Test
    public void testIssueCommand() throws ZosConsoleException {
        zosConsoleCommand.issueCommand();
        Assert.assertEquals("getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommand.getCommand());
    }
    
    @Test
    public void testIssueCommandException() throws ZosmfManagerException, ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        zosConsoleCommand.issueCommand();
    }
    
    @Test
    public void testIssueCommandException1() throws ZosConsoleException, ZosmfException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage( "Unable to issue console command \"" + CONSOLE_COMMAND + "\"");
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        zosConsoleCommand.issueCommand();
    }
    
    @Test
    public void testIssueCommandException2() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage("Console command \"" + CONSOLE_COMMAND + "\" failed. Reason \"reason\"");
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        zosConsoleCommand.issueCommand();
    }
    
    @Test
    public void testIssueCommandConsoleName() throws ZosConsoleException, ZosmfException {
        Whitebox.setInternalState(zosConsoleCommandSpy, "commandImmediateResponse", CONSOLE_RESOPNSE);
    
        Assert.assertEquals("getResponse() should return " + CONSOLE_RESOPNSE, CONSOLE_RESOPNSE, zosConsoleCommandSpy.getResponse());        
    }

    @Test
    public void testRequestResponse() throws ZosConsoleException, ZosmfException {
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        
        Assert.assertEquals("requestResponse() should return the expected response", CONSOLE_RESOPNSE, zosConsoleCommand.requestResponse());
    }

    @Test
    public void testRequestResponseNotFound() throws ZosConsoleException, ZosmfException {
        Whitebox.setInternalState(zosConsoleCommandSpy, "commandDelayedResponse", "NONE");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        
        Assert.assertEquals("requestResponse() should return the expected response", "NONE", zosConsoleCommandSpy.requestResponse());
    }

    @Test
    public void testRequestResponseException() throws ZosConsoleException, ZosmfException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage("exception");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
        
        zosConsoleCommand.requestResponse();
    }

    @Test
    public void testRequestResponseException1() throws ZosConsoleException, ZosmfException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage( "Unable to issue console command \"" + CONSOLE_COMMAND + "\"");
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException("exception"));
        
        zosConsoleCommand.requestResponse();
    }

    @Test
    public void testConsoleName() throws ZosConsoleException {
         Assert.assertEquals("setConsoleName() should return the default console name", "defcn", zosConsoleCommand.consoleName(null));
         Assert.assertEquals("setConsoleName() should return " + CONSOLE_NAME, CONSOLE_NAME, zosConsoleCommand.consoleName(CONSOLE_NAME));
    }

    @Test
    public void testConsoleNameExceptionTooShort() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);        
        String consoleName = "1";
        exceptionRule.expectMessage("Invalid console name \"" + consoleName + "\" must be between 2 and 8 charaters long");
         
        zosConsoleCommand.consoleName(consoleName);
    }


    @Test
    public void testConsoleNameExceptionToLong() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);        
        String consoleName = "123456789";
        exceptionRule.expectMessage("Invalid console name \"" + consoleName + "\" must be between 2 and 8 charaters long");
         
        zosConsoleCommand.consoleName(consoleName);
    }

    @Test
    public void testLogUnableToIsuueCommand() {
        Whitebox.setInternalState(zosConsoleCommandSpy, "command", CONSOLE_COMMAND);
        String response = "Unable to issue console command \"" + CONSOLE_COMMAND + "\"";
        
        Assert.assertEquals("logUnableToIsuueCommand() should return the correct String", response, zosConsoleCommandSpy.logUnableToIsuueCommand());
    }

    @Test
    public void testToString() {
        Whitebox.setInternalState(zosConsoleCommandSpy, "command", CONSOLE_COMMAND);
        String nullString = null;
        Whitebox.setInternalState(zosConsoleCommandSpy, "commandImmediateResponse", nullString);
        Whitebox.setInternalState(zosConsoleCommandSpy, "imageId", nullString);
        String response = "COMMAND=" + CONSOLE_COMMAND;
        Assert.assertEquals("toString() should return the correct String", response, zosConsoleCommandSpy.toString());

        Whitebox.setInternalState(zosConsoleCommandSpy, "imageId", "IMAGE");
        response = response + " IMAGE=IMAGE";
        Assert.assertEquals("toString() should return the correct String", response, zosConsoleCommandSpy.toString());

        Whitebox.setInternalState(zosConsoleCommandSpy, "commandImmediateResponse", CONSOLE_RESOPNSE);
        response = response + " RESPONSE:\n " + CONSOLE_RESOPNSE;
        Assert.assertEquals("toString() should return the correct String", response, zosConsoleCommandSpy.toString());
        
        
    }

    private JsonObject getJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cmd-response", CONSOLE_RESOPNSE);
        jsonObject.addProperty("cmd-response-key", "cmd-response-key");
        jsonObject.addProperty("reason", "reason");
        jsonObject.addProperty("return-code", 99);
        jsonObject.addProperty("reason-code", 99);
        return jsonObject;
    }
}
