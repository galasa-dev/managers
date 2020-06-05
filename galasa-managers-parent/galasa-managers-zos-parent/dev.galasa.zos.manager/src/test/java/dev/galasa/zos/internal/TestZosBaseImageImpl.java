/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.zos.ZosManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractManager.class, ZosIpHostImpl.class})
public class TestZosBaseImageImpl {

    private ZosBaseImageImpl zosBaseImage;
    
    private ZosBaseImageImpl zosBaseImageSpy;

    @Mock
    private ZosManagerImpl zosManagerMock;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Mock
    private IDynamicStatusStoreService dssMock;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    private ICredentialsService credentialsServiceMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String IMAGE_ID = "image";

    private static final String CLUSTER_ID = "cluster";

    private static final String PLEX_ID = "sysplex";

    private static final String DEFAULT_CREDENTIALS_ID = "credentials";

    private static final String IPV4_HOSTNAME = "ipv4.hostname";

    private static final String IPV6_HOSTNAME = "ipv6.hostname";
    
    @Before
    public void setup() throws Exception {
        PowerMockito.when(zosManagerMock.getCPS()).thenReturn(cpsMock);
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(PLEX_ID))).thenReturn(PLEX_ID);
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString())).thenReturn(DEFAULT_CREDENTIALS_ID);
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV4_HOSTNAME))).thenReturn(IPV4_HOSTNAME);
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV6_HOSTNAME))).thenReturn(IPV6_HOSTNAME);
        
        
        PowerMockito.when(zosManagerMock.getDSS()).thenReturn(dssMock);
        PowerMockito.when(zosManagerMock.getFramework()).thenReturn(frameworkMock);
        PowerMockito.when(frameworkMock.getCredentialsService()).thenReturn(credentialsServiceMock);
        
        PowerMockito.mockStatic(AbstractManager.class);
        PowerMockito.doReturn(PLEX_ID).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(PLEX_ID));
        PowerMockito.doReturn(DEFAULT_CREDENTIALS_ID).when(AbstractManager.class, "defaultString", ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString());
        PowerMockito.doReturn(IPV4_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV4_HOSTNAME));
        PowerMockito.doReturn(IPV6_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV6_HOSTNAME));
        
        zosBaseImage = new ZosBaseImageImplExtended(zosManagerMock, IMAGE_ID, CLUSTER_ID);
        zosBaseImageSpy = PowerMockito.spy(zosBaseImage);
    }
    

    @Test
    public void testConstructorException1() throws ZosManagerException, ConfigurationPropertyStoreException {
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(PLEX_ID))).thenThrow(new ConfigurationPropertyStoreException());
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Problem populating Image " + IMAGE_ID + " properties");
        zosBaseImage = new ZosBaseImageImplExtended(zosManagerMock, IMAGE_ID, CLUSTER_ID);
    }
    
    @Test
    public void testConstructorException2() throws ZosManagerException, ConfigurationPropertyStoreException {
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV4_HOSTNAME))).thenThrow(new ConfigurationPropertyStoreException());
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to create the IP Host for the image " + IMAGE_ID);
        zosBaseImage = new ZosBaseImageImplExtended(zosManagerMock, IMAGE_ID, CLUSTER_ID);
    }
    
    @Test
    public void testGetCPS() throws Exception {
        Assert.assertEquals("getCPS() should return the expected value", cpsMock, zosBaseImageSpy.getCPS());
    }
    
    @Test
    public void testGetZosManager() throws Exception {
        Assert.assertEquals("getZosManager() should return the expected value", zosManagerMock, zosBaseImageSpy.getZosManager());
    }
    
    @Test
    public void testGetImageID() throws Exception {
        Assert.assertEquals("getImageID() should return the expected value", IMAGE_ID, zosBaseImageSpy.getImageID());
    }
    
    @Test
    public void testGetSysplexID() throws Exception {
        Assert.assertEquals("getSysplexID() should return the expected value", PLEX_ID, zosBaseImageSpy.getSysplexID());
    }
    
    @Test
    public void testGetClusterID() throws Exception {
        Assert.assertEquals("getClusterID() should return the expected value", CLUSTER_ID, zosBaseImageSpy.getClusterID());
    }
    
    @Test
    public void testGetDefaultHostname() throws Exception {
        Assert.assertEquals("getDefaultHostname() should return the expected value", IPV4_HOSTNAME, zosBaseImageSpy.getDefaultHostname());
    }
    
    @Test
    public void testGetIpHost() throws Exception {
        Assert.assertEquals("getIpHost() should return the expected value", IPV4_HOSTNAME, zosBaseImageSpy.getIpHost().getIpv4Hostname());
    }
    
    @Test
    public void testGetDefaultCredentials() throws Exception {
        ICredentials credentialsMock = Mockito.mock(ICredentials.class);
        Mockito.when(credentialsServiceMock.getCredentials(Mockito.anyString())).thenReturn(credentialsMock);
        Assert.assertEquals("getDefaultCredentials() should return the expected value", credentialsMock, zosBaseImageSpy.getDefaultCredentials());

        Assert.assertEquals("getDefaultCredentials() should return the expected value", credentialsMock, zosBaseImageSpy.getDefaultCredentials());
        
        Whitebox.setInternalState(zosBaseImageSpy, "defaultCedentials", (ICredentials) null);
        Mockito.when(credentialsServiceMock.getCredentials(Mockito.anyString())).thenReturn(null).thenReturn(credentialsMock);
        Assert.assertEquals("getDefaultCredentials() should return the expected value", credentialsMock, zosBaseImageSpy.getDefaultCredentials());      
    }
    
    @Test
    public void testGetDefaultCredentialsException1() throws Exception {        
        Whitebox.setInternalState(zosBaseImageSpy, "defaultCedentials", (ICredentials) null);
        Mockito.when(frameworkMock.getCredentialsService()).thenThrow(new CredentialsException());

        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to acquire the credentials for id " + DEFAULT_CREDENTIALS_ID);
        zosBaseImageSpy.getDefaultCredentials();      
    }
    
    @Test
    public void testGetDefaultCredentialsException2() throws Exception {        
        Whitebox.setInternalState(zosBaseImageSpy, "defaultCedentials", (ICredentials) null);
        Mockito.when(credentialsServiceMock.getCredentials(Mockito.anyString())).thenReturn(null);

        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("zOS Credentials missing for image " + IMAGE_ID + " id " + DEFAULT_CREDENTIALS_ID);
        zosBaseImageSpy.getDefaultCredentials();      
    }
    
    @Test
    public void testToString() throws Exception {
        Assert.assertEquals("toString() should return the expected value", IMAGE_ID, zosBaseImageSpy.toString());
    }
    
    class ZosBaseImageImplExtended extends ZosBaseImageImpl {
        public ZosBaseImageImplExtended(dev.galasa.zos.internal.ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
            super(zosManager, imageId, clusterId);
        }
        
    }
}
