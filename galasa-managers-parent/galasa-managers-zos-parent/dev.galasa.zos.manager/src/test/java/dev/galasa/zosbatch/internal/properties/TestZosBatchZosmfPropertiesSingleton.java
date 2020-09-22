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
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosbatch.ZosBatchManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchZosmfPropertiesSingleton {
    
    private ZosBatchPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosBatchPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosBatchPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosBatchPropertiesSingleton.cps());
    }
    
    @Test
    public void testZosBatchZosmfPropertiesSingleton() throws ZosBatchManagerException {
        singletonInstance = new ZosBatchPropertiesSingleton();
        singletonInstance.activate();
        ZosBatchPropertiesSingleton.setCps(null);
        ZosBatchPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosBatchZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosBatchPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
