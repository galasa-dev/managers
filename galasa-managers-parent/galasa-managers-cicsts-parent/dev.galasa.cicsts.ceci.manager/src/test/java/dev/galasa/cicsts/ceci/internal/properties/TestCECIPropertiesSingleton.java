/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci.internal.properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.cicsts.ceci.CECIManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@RunWith(MockitoJUnitRunner.class)
public class TestCECIPropertiesSingleton {
    
    private CECIPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws CECIManagerException {
        exceptionRule.expect(CECIManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, CECIPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws CECIManagerException {
        exceptionRule.expect(CECIManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        CECIPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, CECIPropertiesSingleton.cps());
    }
    
    @Test
    public void testCECIPropertiesSingleton() throws CECIManagerException {
        singletonInstance = new CECIPropertiesSingleton();
        singletonInstance.activate();
        CECIPropertiesSingleton.setCps(null);
        CECIPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("CECIPropertiesSingleton.cps() should return the mocked cps", cpsMock, CECIPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
