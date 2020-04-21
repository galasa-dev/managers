/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix.ssh.manager.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosUNIXCommandSshPropertiesSingleton {
    
    private ZosUNIXCommandSshPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosUNIXCommandManagerException {
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosUNIXCommandSshPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosUNIXCommandManagerException {
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosUNIXCommandSshPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosUNIXCommandSshPropertiesSingleton.cps());
    }
    
    @Test
    public void testZosUNIXCommandSshPropertiesSingleton() throws ZosUNIXCommandManagerException {
        singletonInstance = new ZosUNIXCommandSshPropertiesSingleton();
        singletonInstance.activate();
        ZosUNIXCommandSshPropertiesSingleton.setCps(null);
        ZosUNIXCommandSshPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosUNIXCommandSshPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosUNIXCommandSshPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
