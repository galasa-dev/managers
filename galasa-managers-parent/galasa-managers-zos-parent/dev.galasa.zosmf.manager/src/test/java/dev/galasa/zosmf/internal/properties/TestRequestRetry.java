/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosmf.internal.properties;

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
import dev.galasa.zosmf.ZosmfManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosmfPropertiesSingleton.class, CpsProperties.class})
public class TestRequestRetry {
    
    private ZosmfPropertiesSingleton properties;
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String SERVERID = "MFSYSA";
    
    private static final String DEFAULT_REQUEST_RETRY = "3";
    
    @Before
    public void setup() throws ConfigurationPropertyStoreException, ZosmfManagerException {
        Mockito.when(configurationPropertyStoreServiceMock.getProperty("command", "request.retry", SERVERID)).thenReturn(DEFAULT_REQUEST_RETRY);
        properties = new ZosmfPropertiesSingleton();
        properties.activate();
        ZosmfPropertiesSingleton.setCps(configurationPropertyStoreServiceMock);       
    }
    
    @Test
    public void testValid() throws Exception {
        int retry = RequestRetry.get(SERVERID);
        
        Assert.assertEquals("Unexpected value returned from RequestRetry.get()", Integer.parseInt(DEFAULT_REQUEST_RETRY), retry);
    }
    
    @Test
    public void testInvalid() throws Exception {
        String invalidValue = "BOB";
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Invalid value given for zosmf.*.request.retry '" + invalidValue + "'");
        
        Mockito.when(configurationPropertyStoreServiceMock.getProperty("command", "request.retry", SERVERID)).thenReturn(invalidValue);

        RequestRetry.get(SERVERID);
    }
    
}
