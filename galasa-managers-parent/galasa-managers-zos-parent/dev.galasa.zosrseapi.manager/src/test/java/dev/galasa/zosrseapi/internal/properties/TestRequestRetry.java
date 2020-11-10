/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosrseapi.RseapiManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RseapiPropertiesSingleton.class, CpsProperties.class})
public class TestRequestRetry {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    private static final String IMAGE_ID = "IMAGE";
    
    private static final int DEFAULT_REQUEST_RETRY = 3;
    
    @Test
    public void testConstructor() {
        RequestRetry requestRetry = new RequestRetry();
        Assert.assertNotNull("Object was not created", requestRetry);
    }
    
    @Test
    public void testNull() throws Exception {
        Assert.assertEquals("Unexpected value returned from RequestRetry.get()", DEFAULT_REQUEST_RETRY, getProperty(null));
    }
    
    @Test
    public void testValid() throws Exception {
        Assert.assertEquals("Unexpected value returned from RequestRetry.get()", 0, getProperty("0"));
        Assert.assertEquals("Unexpected value returned from RequestRetry.get()", Integer.MIN_VALUE, getProperty(String.valueOf(Integer.MIN_VALUE)));
        Assert.assertEquals("Unexpected value returned from RequestRetry.get()", Integer.MAX_VALUE, getProperty(String.valueOf(Integer.MAX_VALUE)));
    }
    
    @Test
    public void testInvalid() throws Exception {
        String expectedMessage = "For input string: \"XXX\"";
        Assert.assertThrows(expectedMessage, NumberFormatException.class, ()->{
        	getProperty("XXX");
        });
    }
    
    @Test
    public void testException() throws Exception {
        String expectedMessage = "Problem asking the CPS for the RSE API request retry property for zOS image " + IMAGE_ID;
        Assert.assertThrows(expectedMessage, RseapiManagerException.class, ()->{
        	getProperty("ANY", true);
        });
    }

    private int getProperty(String value) throws Exception {
        return getProperty(value, false);
    }
    
    private int getProperty(String value, boolean exception) throws Exception {
        PowerMockito.spy(RseapiPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(RseapiPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return RequestRetry.get(IMAGE_ID);
    }
}
