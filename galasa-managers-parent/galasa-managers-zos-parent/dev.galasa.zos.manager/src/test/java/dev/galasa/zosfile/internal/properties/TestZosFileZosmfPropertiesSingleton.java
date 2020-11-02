/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosfile.ZosFileManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosFileZosmfPropertiesSingleton {
    
    private ZosFilePropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws ZosFileManagerException {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, ZosFilePropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws ZosFileManagerException {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        ZosFilePropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, ZosFilePropertiesSingleton.cps());
    }
    
    @Test
    public void testZosFileZosmfPropertiesSingleton() throws ZosFileManagerException {
        singletonInstance = new ZosFilePropertiesSingleton();
        singletonInstance.activate();
        ZosFilePropertiesSingleton.setCps(null);
        ZosFilePropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("ZosFileZosmfPropertiesSingleton.cps() should return the mocked cps", cpsMock, ZosFilePropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
