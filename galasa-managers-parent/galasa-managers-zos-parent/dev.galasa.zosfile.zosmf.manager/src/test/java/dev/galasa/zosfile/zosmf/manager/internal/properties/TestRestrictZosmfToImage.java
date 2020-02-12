/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal.properties;

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
import dev.galasa.zosfile.ZosFileManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosFileZosmfPropertiesSingleton.class, CpsProperties.class})
public class TestRestrictZosmfToImage {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String IMAGE_ID = "IMAGE";
    
    @Test
    public void testConstructor() {
        RestrictZosmfToImage restrictZosmfToImage = new RestrictZosmfToImage();
        Assert.assertNotNull("Object was not created", restrictZosmfToImage);
    }
    
    @Test
    public void testNull() throws Exception {
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", false, getProperty(null));
    }
    
    @Test
    public void testValid() throws Exception {
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", true, getProperty("true"));
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", true, getProperty("TRUE"));
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", true, getProperty("TrUe"));
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", false, getProperty("fasle"));
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", false, getProperty("FALSE"));
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", false, getProperty("FaLsE"));
    }
    
    @Test
    public void testInvalid() throws Exception {
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", false, getProperty("XXX"));
        Assert.assertEquals("Unexpected value returned from RestrictZosmfToImage.get()", false, getProperty("999"));
    }
    
    @Test
    public void testException() throws Exception {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the restrict zOSMF to image property for zOS image " + IMAGE_ID);

        getProperty("ANY", true);
    }

    private boolean getProperty(String value) throws Exception {
        return getProperty(value, false);
    }
    
    private boolean getProperty(String value, boolean exception) throws Exception {
        PowerMockito.spy(ZosFileZosmfPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosFileZosmfPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return RestrictZosmfToImage.get(IMAGE_ID);
    }
}
