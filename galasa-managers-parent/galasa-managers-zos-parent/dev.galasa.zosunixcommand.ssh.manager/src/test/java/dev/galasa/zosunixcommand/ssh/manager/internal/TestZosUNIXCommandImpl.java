/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunixcommand.ssh.manager.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.internal.IpNetworkManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

@RunWith(PowerMockRunner.class)
public class TestZosUNIXCommandImpl {
    
    private static final String FIXED_COMMAND = "command";

    private static final String IMAGE = "image";
    
    private ZosUNIXCommandImpl zosUNIXCommand;
    
    private ZosUNIXCommandImpl zosUNIXCommandSpy;

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
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        ZosUNIXCommandManagerImpl.setIpNetworkManager(ipNetworkManagerMock);
        Mockito.when(ipNetworkManagerMock.getCommandShell(Mockito.any(), Mockito.any())).thenReturn(commandShellMock);
        Mockito.doNothing().when(commandShellMock).reportResultStrings(Mockito.anyBoolean());

        zosUNIXCommand = new ZosUNIXCommandImpl(zosImageMock);
        zosUNIXCommandSpy = Mockito.spy(zosUNIXCommand);
    }
    
    @Test
    public void testConstructorException1() throws ZosManagerException, ZosUNIXCommandException {        
        Mockito.when(zosImageMock.getDefaultCredentials()).thenThrow(new ZosManagerException());
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage("Unable to get default credentials for image " + IMAGE);
        new ZosUNIXCommandImpl(zosImageMock);
    }
    
    @Test
    public void testConstructorException2() throws IpNetworkManagerException, ZosUNIXCommandException {      
        Mockito.when(ipNetworkManagerMock.getCommandShell(Mockito.any(), Mockito.any())).thenThrow(new IpNetworkManagerException());
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage("Unable to get IP Network Command Shell on image " + IMAGE);
        new ZosUNIXCommandImpl(zosImageMock);
        
    }
    
    @Test
    public void testIssueCommand() throws ZosUNIXCommandException, IpNetworkManagerException {
        Mockito.doReturn(FIXED_COMMAND).when(commandShellMock).issueCommand(Mockito.any());
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosUNIXCommandSpy.issueCommand(FIXED_COMMAND));
        
        Mockito.when(commandShellMock.issueCommand(Mockito.any())).thenThrow(new IpNetworkManagerException());
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage("Unable to issue zOS UNIX Command");
        zosUNIXCommandSpy.issueCommand(FIXED_COMMAND);
    }
    
    @Test
    public void testIssueCommandTimout() throws ZosUNIXCommandException, IpNetworkManagerException {
        Mockito.doReturn(FIXED_COMMAND).when(commandShellMock).issueCommand(Mockito.any(), Mockito.anyLong());
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosUNIXCommandSpy.issueCommand(FIXED_COMMAND, 1L));
        
        Mockito.when(commandShellMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenThrow(new IpNetworkManagerException());
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage("Unable to issue zOS UNIX Command");
        zosUNIXCommandSpy.issueCommand(FIXED_COMMAND, 1L);
    }
}