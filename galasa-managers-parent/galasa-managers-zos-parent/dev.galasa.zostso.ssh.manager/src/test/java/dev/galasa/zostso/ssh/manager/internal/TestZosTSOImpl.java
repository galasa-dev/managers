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

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zostso.ZosTSOCommandException;
import dev.galasa.zosunix.IZosUNIX;
import dev.galasa.zosunix.IZosUNIXCommand;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;
import dev.galasa.zosunix.ssh.manager.internal.ZosUNIXCommandManagerImpl;

@RunWith(PowerMockRunner.class)
public class TestZosTSOImpl {
    
    private static final String FIXED_COMMAND = "command";
    
    private ZosTSOImpl zosTSO;
    
    private ZosTSOImpl zosTSOspy;
    
    @Mock
    private ZosUNIXCommandManagerImpl zosUnixManagerMock;
    
    @Mock
    private IZosUNIX zosUNIXMock;
    
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
        Mockito.when(zosUnixManagerMock.getZosUNIX(Mockito.any())).thenReturn(zosUNIXMock);
        Mockito.when(zosUNIXMock.issueCommand(Mockito.any())).thenReturn(zosTSOCommandMock);
        Mockito.when(zosUNIXMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenReturn(zosTSOCommandMock);

        zosTSO = new ZosTSOImpl(zosImageMock);
        zosTSOspy = Mockito.spy(zosTSO);
    }
    
    @Test
    public void testIssueCommand() throws ZosTSOCommandException {
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOspy.issueCommand(FIXED_COMMAND).getCommand());
    }
    
    @Test
    public void testIssueCommandTimout() throws ZosTSOCommandException {
        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOspy.issueCommand(FIXED_COMMAND, 1L).getCommand());
    }
    
    @Test
    public void testNewZosTSOCommandException() throws ZosManagerException {
        Mockito.doThrow(new ZosUNIXCommandManagerException()).when(zosUnixManagerMock).getZosUNIX(zosImageMock);
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage("Unable to issue zOS TSO Command");
        zosTSOspy.newZosTSOCommand(FIXED_COMMAND);
    }
}