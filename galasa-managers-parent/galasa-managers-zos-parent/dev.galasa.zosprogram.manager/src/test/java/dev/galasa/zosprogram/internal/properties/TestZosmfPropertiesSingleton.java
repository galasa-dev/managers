/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosprogram.ZosProgramManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosmfPropertiesSingleton {
    
    private ZosProgramPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosProgramManagerException {
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosProgramPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosProgramManagerException {
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosProgramPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosProgramPropertiesSingleton.cps());
    }
    
    @Test
    public void testZosProgramPropertiesSingleton() throws ZosProgramManagerException {
        singletonInstance = new ZosProgramPropertiesSingleton();
        singletonInstance.activate();
        ZosProgramPropertiesSingleton.setCps(null);
        ZosProgramPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosProgramPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosProgramPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
