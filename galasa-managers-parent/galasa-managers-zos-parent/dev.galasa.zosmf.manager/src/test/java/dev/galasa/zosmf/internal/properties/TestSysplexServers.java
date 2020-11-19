/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosmf.internal.properties;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosmf.ZosmfManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosmfPropertiesSingleton.class, CpsProperties.class})
public class TestSysplexServers {
    
    private ZosmfPropertiesSingleton properties;
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    @Mock
    private IZosImage zosImage;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String SYSPLEXID = "PLEX1";
    
    private static final String DEMO_SERVER1 = "MFSYSA";
    private static final String DEMO_SERVER2 = "MFSYSB";
    private static final String DEMO_SERVERS = DEMO_SERVER1 + "," + DEMO_SERVER2;
    
    @Before
    public void setup() throws ConfigurationPropertyStoreException, ZosmfManagerException {
        Mockito.when(configurationPropertyStoreServiceMock.getProperty("sysplex", "default.servers", SYSPLEXID)).thenReturn(DEMO_SERVERS);
        Mockito.when(zosImage.getSysplexID()).thenReturn(SYSPLEXID);
        properties = new ZosmfPropertiesSingleton();
        properties.activate();
        ZosmfPropertiesSingleton.setCps(configurationPropertyStoreServiceMock);       
    }
    
    
    @Test
    public void testValid() throws Exception {
        List<String> servers = SysplexServers.get(zosImage);
        
        Assert.assertEquals("Incorrect number of servers returned", 2, servers.size());
        Assert.assertEquals("Incorrect server 1", DEMO_SERVER1, servers.get(0));
        Assert.assertEquals("Incorrect server 2", DEMO_SERVER2, servers.get(1));
    }
    
    @Test
    public void testException() throws Exception {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the zOSMF servers property for zOS sysplex " + SYSPLEXID);

        Mockito.when(configurationPropertyStoreServiceMock.getProperty("sysplex", "default.servers", SYSPLEXID)).thenThrow(new ConfigurationPropertyStoreException("Test exception"));

        SysplexServers.get(zosImage);
    }

}
