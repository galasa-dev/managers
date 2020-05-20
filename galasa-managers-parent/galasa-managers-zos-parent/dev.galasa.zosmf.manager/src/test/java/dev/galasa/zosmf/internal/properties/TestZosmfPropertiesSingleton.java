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
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosmf.ZosmfManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosmfPropertiesSingleton {
    
    private ZosmfPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosmfManagerException {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosmfPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosmfManagerException {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosmfPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosmfPropertiesSingleton.cps());
    }
    
    @Test
    public void testZosmfPropertiesSingleton() throws ZosmfManagerException {
        singletonInstance = new ZosmfPropertiesSingleton();
        singletonInstance.activate();
        ZosmfPropertiesSingleton.setCps(null);
        ZosmfPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosmfPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
