/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.internal.properties;

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
import dev.galasa.zosbatch.ZosBatchManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosBatchPropertiesSingleton.class, CpsProperties.class})
public class TestJobWaitTimeout {
    
    @Mock
    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String IMAGE_ID = "IMAGE";
    
    private static final int DEFAULT_JOB_WAIT_TIMEOUT = 5 * 60;
    
    @Test
    public void testConstructor() {
        JobWaitTimeout jobWaitTimeout = new JobWaitTimeout();
        Assert.assertNotNull("Object was not created", jobWaitTimeout);
    }
    
    @Test
    public void testNull() throws Exception {
        Assert.assertEquals("Unexpected value returned from JobWaitTimeout.get()", DEFAULT_JOB_WAIT_TIMEOUT, getProperty(null));
    }
    
    @Test
    public void testValid() throws Exception {
        Assert.assertEquals("Unexpected value returned from JobWaitTimeout.get()", 0, getProperty("0"));
        Assert.assertEquals("Unexpected value returned from JobWaitTimeout.get()", 99, getProperty("99"));
        Assert.assertEquals("Unexpected value returned from JobWaitTimeout.get()", 99, getProperty("+99"));
    }
    
    @Test
    public void testNegative() throws Exception {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Batch job wait timeout property must be a positive integer");
        
        getProperty("-99");
    }

    @Test
    public void testNonInteger() throws Exception {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the batch job wait timeout property for zOS image " + IMAGE_ID);
        
        getProperty("99.99");
    }

    @Test
    public void testNonNumeric() throws Exception {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the batch job wait timeout property for zOS image " + IMAGE_ID);

        getProperty("XXX");
    }
    
    @Test
    public void testException() throws Exception {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Problem asking the CPS for the batch job wait timeout property for zOS image " + IMAGE_ID);
        
        getProperty("ANY", true);
    }
    
    private int getProperty(String value) throws Exception {
        return getProperty(value, false);
    }
    
    private int getProperty(String value, boolean exception) throws Exception {
        PowerMockito.spy(ZosBatchPropertiesSingleton.class);
        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosBatchPropertiesSingleton.class, "cps");
        PowerMockito.spy(CpsProperties.class);
        
        if (!exception) {
            PowerMockito.doReturn(value).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());            
        } else {
            PowerMockito.doThrow(new ConfigurationPropertyStoreException()).when(CpsProperties.class, "getStringNulled", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        }
        
        return JobWaitTimeout.get(IMAGE_ID);
    }
}
