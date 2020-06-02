/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosmf.internal.properties;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosmfPropertiesSingleton.class, CpsProperties.class})
public class TestServerImages {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String CLUSTER_ID = "CLUSTER";
    
    @Test
    public void testConstructor() {
        ServerImages serverImages = new ServerImages();
        Assert.assertNotNull("Object was not created", serverImages);
    }
    
    @Test
    public void testNull() throws Exception {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Value for zOSMF server images property not configured for zOS cluster "  + CLUSTER_ID);
        getProperty(null);
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
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the zOSMF server images property for zOS cluster " + CLUSTER_ID);
        
        getProperty("ANY", true);
    }

    private List<String> getProperty(String value) throws Exception {
        return getProperty(value, false);
    }
    
    private List<String> getProperty(String value, boolean exception) throws Exception {
        PowerMockito.spy(ZosmfPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosmfPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return ServerImages.get(CLUSTER_ID);
    }
}
