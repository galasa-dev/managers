/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal.properties;

import java.util.Arrays;
import java.util.List;

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
public class TestServerImages {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    private static final String CLUSTER_ID = "CLUSTER";
    
    @Test
    public void testConstructor() {
        ServerImages serverImages = new ServerImages();
        Assert.assertNotNull("Object was not created", serverImages);
    }
    
    @Test
    public void testNull() throws Exception {
        String expectedMessage = "Value for RSE API server images property not configured for zOS cluster "  + CLUSTER_ID;
        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
        	getProperty(null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testEmpty() throws Exception {
        Assert.assertEquals("Unexpected value returned from ServerImages.get()", Arrays.asList(""), getProperty(""));
    }
    
    @Test
    public void testValid() throws Exception {
        List<String> serverImages = Arrays.asList("image1", "image2");
        Assert.assertEquals("Unexpected value returned from ServerImages.get()", serverImages, getProperty("image1,image2"));
    }
    
    @Test
    public void testException() throws Exception {
        String expectedMessage = "Problem asking the CPS for the RSE API server images property for zOS cluster " + CLUSTER_ID;
        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
        	getProperty("ANY", true);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private List<String> getProperty(String value) throws Exception {
        return getProperty(value, false);
    }
    
    private List<String> getProperty(String value, boolean exception) throws Exception {
        PowerMockito.spy(RseapiPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(RseapiPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return ServerImages.get(CLUSTER_ID);
    }
}
