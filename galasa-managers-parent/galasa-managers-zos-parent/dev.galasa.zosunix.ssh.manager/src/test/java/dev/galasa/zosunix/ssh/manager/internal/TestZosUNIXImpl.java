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

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.internal.IpNetworkManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosunix.ZosUNIXCommandException;

@RunWith(PowerMockRunner.class)
public class TestZosUNIXImpl {
    
    private static final String FIXED_COMMAND = "command";
    
    private ZosUNIXImpl zosUNIX;
    
    private ZosUNIXImpl zosUNIXspy;

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

        zosUNIX = new ZosUNIXImpl(zosImageMock);
        zosUNIXspy = Mockito.spy(zosUNIX);
    }
    
    @Test
    public void testIssueCommand() throws ZosUNIXCommandException {
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosUNIXspy.issueCommand(FIXED_COMMAND).getCommand());
    }
    
    @Test
    public void testIssueCommandTimout() throws ZosUNIXCommandException {
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosUNIXspy.issueCommand(FIXED_COMMAND, 1L).getCommand());
    }
    
    @Test
    public void testNewZosUNIXCommandException() throws ZosManagerException {
        Mockito.doThrow(new ZosManagerException()).when(zosImageMock).getDefaultCredentials();
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage("Unable to issue command zOS UNIX Command");
        zosUNIXspy.newZosUNIXCommand(FIXED_COMMAND);
    }
}