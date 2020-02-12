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

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.IZosConsoleCommand;
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
public class TestZosConsoleImpl {
    
    private ZosConsoleImpl zosConsole;

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
    
    @Before
    public void setup() throws Exception {
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        PowerMockito.mockStatic(RestrictToImage.class);
        Mockito.when(RestrictToImage.get(Mockito.any())).thenReturn(true);

        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictToImage.get(zosImageMock.getImageID()))).thenReturn(zosmfApiProcessorMock);
        ZosConsoleManagerImpl.setZosmfManager(zosmfManagerMock);
        
        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(zosmfResponseMock);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cmd-response", "cmd-response");
        jsonObject.addProperty("cmd-response-key", "cmd-response-key");
        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject );
        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        zosConsole = new ZosConsoleImpl(zosImageMock);
    }
    
    @Test
    public void testIssueCommand() throws ZosConsoleException {    
        IZosConsoleCommand zosConsoleCommand = zosConsole.issueCommand(CONSOLE_COMMAND );

        Assert.assertEquals("IZosConsoleCommand.getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommand.getCommand());
    }
    
    @Test
    public void testIssueCommandConsoleName() throws ZosConsoleException, ZosmfException {        
        IZosConsoleCommand zosConsoleCommand = zosConsole.issueCommand(CONSOLE_COMMAND, CONSOLE_NAME);

        Assert.assertEquals("IZosConsoleCommand.getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommand.getCommand());        
    }

    @Test
    public void testIssueCommandConsoleNameException() throws ZosmfManagerException, ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage("Unable to issue console command");
        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictToImage.get(zosImageMock.getImageID()))).thenThrow(new ZosmfManagerException("exception"));
                
        zosConsole.issueCommand("command", "name");
    }
}
