/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosconsole.zosmf.manager.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosconsole.ZosConsoleManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosConsoleZosmfPropertiesSingleton {
    
    private ZosConsoleZosmfPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosConsoleZosmfPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosConsoleZosmfPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosConsoleZosmfPropertiesSingleton.cps());
    }
    
    @Test
    public void testZosConsoleZosmfPropertiesSingleton() throws ZosConsoleManagerException {
        singletonInstance = new ZosConsoleZosmfPropertiesSingleton();
        singletonInstance.activate();
        ZosConsoleZosmfPropertiesSingleton.setCps(null);
        ZosConsoleZosmfPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosConsoleZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosConsoleZosmfPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
