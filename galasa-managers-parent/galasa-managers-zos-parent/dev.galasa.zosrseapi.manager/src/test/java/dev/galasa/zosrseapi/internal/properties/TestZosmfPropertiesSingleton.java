/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosrseapi.RseapiManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosmfPropertiesSingleton {
    
    private RseapiPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Test
    public void testCpsException() throws RseapiManagerException {
        String expectedMessage = "Attempt to access manager CPS before it has been initialised";
        Assert.assertThrows(expectedMessage, RseapiManagerException.class, ()->{
        	Assert.assertEquals("Exception", null, RseapiPropertiesSingleton.cps());
        });
    }
    
    @Test
    public void testSetCpsException() throws RseapiManagerException {
        String expectedMessage = "Attempt to set manager CPS before instance created";
        Assert.assertThrows(expectedMessage, RseapiManagerException.class, ()->{
        	RseapiPropertiesSingleton.setCps(cpsMock);
        });
    }
    
    @Test
    public void testZosmfPropertiesSingleton() throws RseapiManagerException {
        singletonInstance = new RseapiPropertiesSingleton();
        singletonInstance.activate();
        RseapiPropertiesSingleton.setCps(null);
        RseapiPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, RseapiPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
