/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.ZosBatchZosmfPropertiesSingleton;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchZosmfPropertiesSingleton {
    
    private ZosBatchZosmfPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosBatchZosmfPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosBatchZosmfPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosBatchZosmfPropertiesSingleton.cps());
    }
    
    @Test
    public void testZosBatchZosmfPropertiesSingleton() throws ZosBatchManagerException {
        singletonInstance = new ZosBatchZosmfPropertiesSingleton();
        singletonInstance.activate();
        ZosBatchZosmfPropertiesSingleton.setCps(null);
        ZosBatchZosmfPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosBatchZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosBatchZosmfPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
