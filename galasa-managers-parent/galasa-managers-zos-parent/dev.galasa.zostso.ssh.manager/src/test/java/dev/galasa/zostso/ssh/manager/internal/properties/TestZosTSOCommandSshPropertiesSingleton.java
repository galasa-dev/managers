/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso.ssh.manager.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zostso.ZosTSOCommandManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosTSOCommandSshPropertiesSingleton {
    
    private ZosTSOCommandSshPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosTSOCommandManagerException {
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosTSOCommandSshPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosTSOCommandManagerException {
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosTSOCommandSshPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosTSOCommandSshPropertiesSingleton.cps());
    }
    
    @Test
    public void ZosTSOCommandSshPropertiesSingleton() throws ZosTSOCommandManagerException {
        singletonInstance = new ZosTSOCommandSshPropertiesSingleton();
        singletonInstance.activate();
        ZosTSOCommandSshPropertiesSingleton.setCps(null);
        ZosTSOCommandSshPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosTSOCommandSshPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosTSOCommandSshPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
