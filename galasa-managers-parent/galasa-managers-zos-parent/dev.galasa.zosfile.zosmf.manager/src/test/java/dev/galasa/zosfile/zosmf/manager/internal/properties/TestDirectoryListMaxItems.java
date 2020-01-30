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
public class TestDirectoryListMaxItems {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String IMAGE_ID = "IMAGE";
    
    private static final int MAX_ITEMS = 1000;
    
    @Test
    public void testConstructor() {
    	DirectoryListMaxItems directoryListMaxItems = new DirectoryListMaxItems();
        Assert.assertNotNull("Object was not created", directoryListMaxItems);
    }
    
    @Test
    public void testNull() throws Exception {
        Assert.assertEquals("Unexpected value returned from DirectoryListMaxItems.get()", MAX_ITEMS, getProperty(null));
    }
    
    @Test
    public void testValid() throws Exception {
        Assert.assertEquals("Unexpected value returned from DirectoryListMaxItems.get()", 99, getProperty("99"));
        Assert.assertEquals("Unexpected value returned from DirectoryListMaxItems.get()", 99, getProperty("+99"));
    }
    
    @Test
	public void testNegative() throws Exception {
	    exceptionRule.expect(ZosFileManagerException.class);
	    exceptionRule.expectMessage("Directory list max items property must be greater than 0");
	    
	    getProperty("-99");
	}
    
    @Test
	public void testZero() throws Exception {
	    exceptionRule.expect(ZosFileManagerException.class);
	    exceptionRule.expectMessage("Directory list max items property must be greater than 0");
	    
	    getProperty("0");
	}

	@Test
	public void testNonInteger() throws Exception {
	    exceptionRule.expect(ZosFileManagerException.class);
	    exceptionRule.expectMessage("Problem asking the CPS for the directory list max items property for zOS image " + IMAGE_ID);
	    
	    getProperty("99.99");
	}

	@Test
    public void testNonNumeric() throws Exception {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the directory list max items property for zOS image " + IMAGE_ID);

        getProperty("XXX");
    }
    
    @Test
    public void testException() throws Exception {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the directory list max items property for zOS image " + IMAGE_ID);

        getProperty(null, true);
    }

    private int getProperty(String i) throws Exception {
        return getProperty(i, false);
    }
    
    private int getProperty(String i, boolean exception) throws Exception {
        PowerMockito.spy(ZosFileZosmfPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosFileZosmfPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(i).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return DirectoryListMaxItems.get(IMAGE_ID);
    }
}
