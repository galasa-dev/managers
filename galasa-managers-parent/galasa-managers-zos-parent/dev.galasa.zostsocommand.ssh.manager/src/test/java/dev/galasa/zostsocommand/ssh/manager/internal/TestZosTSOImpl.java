/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostsocommand.ssh.manager.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zostsocommand.ZosTSOCommandException;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandManagerImpl;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;
import dev.galasa.zosunixcommand.ZosUNIXCommandManagerException;

@RunWith(PowerMockRunner.class)
public class TestZosTSOImpl {
    
    private static final String FIXED_COMMAND = "command";
    
    private ZosTSOCommandImpl zosTSOCommand;
    
    private ZosTSOCommandImpl zosTSOCommandSpy;
    
    @Mock
    private ZosUNIXCommandManagerImpl zosUnixManagerMock;
    
    @Mock
    private IZosUNIXCommand zosUNIXCommandMock;
    
    @Mock
    private IZosUNIXCommand zosTSOCommandMock;

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
        ZosTSOCommandManagerImpl.setZosUnixCommandManager(zosUnixManagerMock);
        Mockito.when(zosUnixManagerMock.getZosUNIXCommand(Mockito.any())).thenReturn(zosUNIXCommandMock);
        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any())).thenReturn(FIXED_COMMAND);
        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenReturn(FIXED_COMMAND);

        zosTSOCommand = new ZosTSOCommandImpl(zosImageMock);
        zosTSOCommandSpy = Mockito.spy(zosTSOCommand);
    }
    
    @Test
    public void testConstructerException() throws ZosUNIXCommandManagerException, ZosTSOCommandManagerException {
        Mockito.when(zosUnixManagerMock.getZosUNIXCommand(Mockito.any())).thenThrow(new ZosUNIXCommandManagerException());
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage("Unable to get zOS UNIX Command instance");
        new ZosTSOCommandImpl(zosImageMock);
    }
    
    @Test
    public void testIssueCommand() throws ZosTSOCommandException, ZosUNIXCommandException {
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOCommandSpy.issueCommand(FIXED_COMMAND));
        
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOCommandSpy.issueCommand("\"" + FIXED_COMMAND + "\""));

        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any())).thenThrow(new ZosUNIXCommandException());
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage("Unable to issue zOS TSO Command");
        zosTSOCommandSpy.issueCommand(FIXED_COMMAND);
    }
    
    @Test
    public void testIssueCommandTimout() throws ZosTSOCommandException, ZosUNIXCommandException {
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOCommandSpy.issueCommand(FIXED_COMMAND, 1L));

        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenThrow(new ZosUNIXCommandException());
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage("Unable to issue zOS TSO Command");
        zosTSOCommandSpy.issueCommand(FIXED_COMMAND, 1L);
    }
}