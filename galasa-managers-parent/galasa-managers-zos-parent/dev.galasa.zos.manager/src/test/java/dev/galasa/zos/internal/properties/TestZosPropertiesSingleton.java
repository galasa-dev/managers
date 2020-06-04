/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zos.ZosManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosPropertiesSingleton {
    
    private ZosPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosManagerException {
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosManagerException {
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosPropertiesSingleton.cps());
    }
    
    @Test
    public void testZosConsoleZosmfPropertiesSingleton() throws ZosManagerException {
        singletonInstance = new ZosPropertiesSingleton();
        singletonInstance.activate();
        ZosPropertiesSingleton.setCps(null);
        ZosPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosConsoleZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
