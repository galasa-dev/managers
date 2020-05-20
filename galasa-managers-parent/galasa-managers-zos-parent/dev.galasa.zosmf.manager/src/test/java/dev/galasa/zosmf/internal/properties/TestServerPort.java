/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosmf.internal.properties;

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
public class TestServerPort {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String IMAGE_ID = "IMAGE";
    
    @Test
    public void testConstructor() {
        ServerPort serverPort = new ServerPort();
        Assert.assertNotNull("Object was not created", serverPort);
    }
    
    @Test
    public void testNull() throws Exception {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Value for zOSMF server port not configured for zOS image " + IMAGE_ID);
        getProperty(null);
    }
    
    @Test
    public void testValid() throws Exception {
        Assert.assertEquals("Unexpected value returned from ServerPort.get()", "1024", getProperty("1024"));
    }
    
    @Test
    public void testInvalidString() throws Exception {
        exceptionRule.expect(NumberFormatException.class);
        exceptionRule.expectMessage("For input string: \"XXX\"");
        
        getProperty("XXX");
    }
    
    @Test
    public void testInvalidTooSmall() throws Exception {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Invalid value (-1) for zOSMF server port property for zOS image "  + IMAGE_ID + ". Range  0-65535");
        getProperty("-1");
    }
    
    @Test
    public void testInvalidTooBig() throws Exception {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Invalid value (65536) for zOSMF server port property for zOS image "  + IMAGE_ID + ". Range  0-65535");
        getProperty("65536");
    }
    
    @Test
    public void testException() throws Exception {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the zOSMF server port property for zOS image " + IMAGE_ID);
        
        getProperty("ANY", true);
    }

    private String getProperty(String value) throws Exception {
        return getProperty(value, false);
    }
    
    private String getProperty(String value, boolean exception) throws Exception {
        PowerMockito.spy(ZosmfPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosmfPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return ServerPort.get(IMAGE_ID);
    }
}
