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
public class TestServerPort {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    private static final String IMAGE_ID = "IMAGE";
    
    @Test
    public void testConstructor() {
        ServerPort serverPort = new ServerPort();
        Assert.assertNotNull("Object was not created", serverPort);
    }
    
    @Test
    public void testValid() throws Exception {
        Assert.assertEquals("Unexpected value returned from ServerPort.get()", "1024", getProperty("1024"));
        Assert.assertEquals("Unexpected value returned from ServerPort.get()", "6800", getProperty(null));
    }
    
    @Test
    public void testInvalidString() throws Exception {
        String expectedMessage = "For input string: \"XXX\"";
        NumberFormatException expectedException = Assert.assertThrows("expected exception should be thrown", NumberFormatException.class, ()->{
        	getProperty("XXX");
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testInvalidTooSmall() throws Exception {
        String expectedMessage = "Invalid value (-1) for RSE API server port property for zOS image "  + IMAGE_ID + ". Range  0-65535";
        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
        	getProperty("-1");
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testInvalidTooBig() throws Exception {
        String expectedMessage = "Invalid value (65536) for RSE API server port property for zOS image "  + IMAGE_ID + ". Range  0-65535";
        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
        	getProperty("65536");
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testException() throws Exception {
        String expectedMessage = "Problem asking the CPS for the RSE API server port property for zOS image " + IMAGE_ID;
        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
        	getProperty("ANY", true);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private String getProperty(String value) throws Exception {
        return getProperty(value, false);
    }
    
    private String getProperty(String value, boolean exception) throws Exception {
        PowerMockito.spy(RseapiPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(RseapiPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return ServerPort.get(IMAGE_ID);
    }
}
