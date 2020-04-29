/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso.ssh.manager.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zostso.ZosTSOCommandException;
import dev.galasa.zostso.ZosTSOCommandManagerException;
import dev.galasa.zosunix.IZosUNIX;
import dev.galasa.zosunix.IZosUNIXCommand;
import dev.galasa.zosunix.ZosUNIXCommandException;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;
import dev.galasa.zosunix.ssh.manager.internal.ZosUNIXCommandManagerImpl;

@RunWith(PowerMockRunner.class)
public class TestZosTSOCommandImpl {
    
    private static final String FIXED_COMMAND = "command";
    
    private static final String FIXED_RESPONSE = "response";

    private static final String FIXED_IMAGID = "image";

    private ZosTSOCommandImpl zosTSOCommand;
    
    private ZosTSOCommandImpl zosTSOCommandSpy;
    
    @Mock
    private ZosUNIXCommandManagerImpl zosUnixManagerMock;
    
    @Mock
    private IZosUNIX zosUNIXMock;
    
    @Mock
    private IZosUNIXCommand zosUNIXCommandMock;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private ICredentials credentialsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        ZosTSOCommandManagerImpl.setZosUnixCommandManager(zosUnixManagerMock);
        Mockito.when(zosUnixManagerMock.getZosUNIX(Mockito.any())).thenReturn(zosUNIXMock);
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(FIXED_IMAGID);
        Mockito.when(zosImageMock.getDefaultCredentials()).thenReturn(credentialsMock);
        
        zosTSOCommand = new ZosTSOCommandImpl(FIXED_COMMAND, zosImageMock);
        zosTSOCommandSpy = Mockito.spy(zosTSOCommand);
    }
    
    @Test
    public void testConstructorException1() throws ManagerException {
        Mockito.doThrow(new ZosUNIXCommandManagerException()).when(zosUnixManagerMock).getZosUNIX(Mockito.any());
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage("Unable to get zOS UNIX Command Manager for image " + FIXED_IMAGID);
        new ZosTSOCommandImpl(FIXED_COMMAND, zosImageMock);
    }
    
    @Test
    public void testIssueCommand() throws ZosUNIXCommandException, ZosTSOCommandException {
        Assert.assertEquals("Error in issueCommand() method", zosTSOCommandSpy, zosTSOCommandSpy.issueCommand());
    }
    
    @Test
    public void testIssueCommandException() throws ZosUNIXCommandException, ZosTSOCommandException {
        Mockito.when(zosUNIXMock.issueCommand(Mockito.any())).thenThrow(new ZosUNIXCommandException());
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage("Unable to issue zOS TSO Command");
        zosTSOCommandSpy.issueCommand();
    }
    
    @Test
    public void testIssueCommandTimeout() throws ZosUNIXCommandException, ZosTSOCommandException {
        Mockito.when(zosUNIXMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenReturn(zosUNIXCommandMock);
        Assert.assertEquals("Error in issueCommand() method", zosTSOCommandSpy, zosTSOCommandSpy.issueCommand(1L));
    }
    
    @Test
    public void testIssueCommandTimeoutException() throws ZosUNIXCommandException, ZosTSOCommandException {
        Mockito.when(zosUNIXMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenThrow(new ZosUNIXCommandException());
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage("Unable to issue zOS TSO Command");
        zosTSOCommandSpy.issueCommand(1L);
    }
    
    @Test
    public void testGetCommand() {
        Assert.assertEquals("Error in getCommand() method", FIXED_COMMAND, zosTSOCommandSpy.getCommand());
    }
    
    @Test
    public void testGetResponse() {
        Mockito.when(zosUNIXCommandMock.getResponse()).thenReturn(FIXED_RESPONSE);
        Whitebox.setInternalState(zosTSOCommandSpy, "zosUnixcommand", zosUNIXCommandMock);
        Assert.assertEquals("Error in getResponse() method", FIXED_RESPONSE, zosTSOCommandSpy.getResponse());
    }
    
    @Test
    public void testToString() throws ZosUNIXCommandException, ZosTSOCommandException {
        Mockito.when(zosUNIXCommandMock.toString()).thenReturn("COMMAND=" + FIXED_COMMAND + " IMAGE=" + FIXED_IMAGID + " RESPONSE:\n " + FIXED_RESPONSE);
        Whitebox.setInternalState(zosTSOCommandSpy, "zosUnixcommand", zosUNIXCommandMock);
        String response = "COMMAND=" + FIXED_COMMAND + " IMAGE=" + FIXED_IMAGID + " RESPONSE:\n " + FIXED_RESPONSE;
        Assert.assertEquals("Error in toString() method", response, zosTSOCommandSpy.toString());
        
        ZosTSOCommandImpl nullZosTSOCommandImpl = null;
        Whitebox.setInternalState(zosTSOCommandSpy, "zosUnixcommand", nullZosTSOCommandImpl);
        
        response = "COMMAND=" + FIXED_COMMAND + " IMAGE=" + FIXED_IMAGID;
        Assert.assertEquals("Error in toString() method", response, zosTSOCommandSpy.issueCommand().toString());
        
        IZosImage nullImage = null;
        Whitebox.setInternalState(zosTSOCommandSpy, "image", nullImage);
        response = "COMMAND=" + FIXED_COMMAND;
        Assert.assertEquals("Error in toString() method", response, zosTSOCommandSpy.issueCommand().toString());
    }
    
    @Test
    public void testBuildCommand() {
        String builtCommand = "tsocmd \"" + FIXED_COMMAND + "\"";
        Whitebox.setInternalState(zosTSOCommandSpy, "command", FIXED_COMMAND);
        Assert.assertEquals("Error in buildCommand() method", builtCommand, zosTSOCommandSpy.buildCommand());

        Whitebox.setInternalState(zosTSOCommandSpy, "command", "\"" + FIXED_COMMAND + "\"");
        Assert.assertEquals("Error in buildCommand() method", builtCommand, zosTSOCommandSpy.buildCommand());
    }
}