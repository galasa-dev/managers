/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix.ssh.manager.internal;

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
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.internal.IpNetworkManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosunix.ZosUNIXCommandException;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;

@RunWith(PowerMockRunner.class)
public class TestZosUNIXCommandImpl {
    
    private static final String FIXED_COMMAND = "command";
    
    private static final String FIXED_RESPONSE = "response";

    private static final String FIXED_IMAGID = "image";

    private ZosUNIXCommandImpl zosUnixCommand;
    
    private ZosUNIXCommandImpl zosUnixCommandSpy;
    
    @Mock
    private IpNetworkManagerImpl ipNetworkManagerMock;
    
    @Mock
    private ICommandShell commandShellMock;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private ICredentials credentialsMock;
    
    @Mock
    private IIpHost hostMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        ZosUNIXCommandManagerImpl.setIpNetworkManager(ipNetworkManagerMock);
        Mockito.when(ipNetworkManagerMock.getCommandShell(Mockito.any(), Mockito.any())).thenReturn(commandShellMock);
        Mockito.doNothing().when(commandShellMock).reportResultStrings(Mockito.anyBoolean());
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(FIXED_IMAGID);
        Mockito.when(zosImageMock.getIpHost()).thenReturn(hostMock);
        Mockito.when(zosImageMock.getDefaultCredentials()).thenReturn(credentialsMock);
        
        zosUnixCommand = new ZosUNIXCommandImpl(FIXED_COMMAND, zosImageMock);
        zosUnixCommandSpy = Mockito.spy(zosUnixCommand);
    }
    
    @Test
    public void testConstructorException1() throws ManagerException {
        Mockito.doThrow(new ZosManagerException()).when(zosImageMock).getDefaultCredentials();
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage("Unable to get default credentials for image " + FIXED_IMAGID);
        new ZosUNIXCommandImpl(FIXED_COMMAND, zosImageMock);
    }
    
    @Test
    public void testConstructorException2() throws ManagerException {
        Mockito.doThrow(new IpNetworkManagerException()).when(ipNetworkManagerMock).getCommandShell(Mockito.any(), Mockito.any());
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage("Unable to get IP Network Command Shell on image" + FIXED_IMAGID);
        new ZosUNIXCommandImpl(FIXED_COMMAND, zosImageMock);
    }
    
    @Test
    public void testIssueCommand() throws IpNetworkManagerException, ZosUNIXCommandException {
        Mockito.when(commandShellMock.issueCommand(Mockito.any())).thenReturn(FIXED_RESPONSE);
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosUnixCommandSpy.issueCommand().getCommand());
    }
    
    @Test
    public void testIssueCommandException() throws IpNetworkManagerException, ZosUNIXCommandException {
        Mockito.when(commandShellMock.issueCommand(Mockito.any())).thenThrow(new IpNetworkManagerException());
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage("Unable to issue zOS UNIX Command");
        zosUnixCommandSpy.issueCommand();
    }
    
    @Test
    public void testIssueCommandTimeout() throws IpNetworkManagerException, ZosUNIXCommandException {
        Mockito.when(commandShellMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenReturn(FIXED_RESPONSE);
        Assert.assertEquals("Error in issueCommand() method", FIXED_RESPONSE, zosUnixCommandSpy.issueCommand(1L).getResponse());
    }
    
    @Test
    public void testIssueCommandTimeoutException() throws IpNetworkManagerException, ZosUNIXCommandException {
        Mockito.when(commandShellMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenThrow(new IpNetworkManagerException());
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage("Unable to issue command zOS UNIX Command");
        zosUnixCommandSpy.issueCommand(1L);
    }
    
    @Test
    public void testToString() throws ZosUNIXCommandException, IpNetworkManagerException {
        String nullString = null;
        Whitebox.setInternalState(zosUnixCommandSpy, "commandResponse", nullString);
        String response = "COMMAND=" + FIXED_COMMAND + " IMAGE=" + FIXED_IMAGID;
        Assert.assertEquals("Error in toString() method", response, zosUnixCommandSpy.toString());
        
        Mockito.when(commandShellMock.issueCommand(Mockito.any())).thenReturn(FIXED_RESPONSE);
        response = "COMMAND=" + FIXED_COMMAND + " IMAGE=" + FIXED_IMAGID + " RESPONSE:\n " + FIXED_RESPONSE;
        Assert.assertEquals("Error in toString() method", response, zosUnixCommandSpy.issueCommand().toString());

        Whitebox.setInternalState(zosUnixCommandSpy, "commandResponse", nullString);
        IZosImage nullImage = null;
        Whitebox.setInternalState(zosUnixCommandSpy, "image", nullImage);
        response = "COMMAND=" + FIXED_COMMAND;
        Assert.assertEquals("Error in toString() method", response, zosUnixCommandSpy.toString());
        response = "COMMAND=" + FIXED_COMMAND + " RESPONSE:\n " + FIXED_RESPONSE;
        Assert.assertEquals("Error in toString() method", response, zosUnixCommandSpy.issueCommand().toString());
    }
}