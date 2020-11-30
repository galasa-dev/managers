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

import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@RunWith(MockitoJUnitRunner.class)
public class TestCECIPropertiesSingleton {
    
    private CECIPropertiesSingleton singletonInstance;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Test
    public void testCpsException() throws CeciManagerException {
        exceptionRule.expect(CeciManagerException.class);
        exceptionRule.expectMessage("Attempt to access manager CPS before it has been initialised");
        Assert.assertEquals("Exception", null, CECIPropertiesSingleton.cps());
    }
    
    @Test
    public void testSetCpsException() throws CeciManagerException {
        exceptionRule.expect(CeciManagerException.class);
        exceptionRule.expectMessage("Attempt to set manager CPS before instance created");
        CECIPropertiesSingleton.setCps(cpsMock);
        Assert.assertEquals("Exception", null, CECIPropertiesSingleton.cps());
    }
    
    @Test
    public void testCECIPropertiesSingleton() throws CeciManagerException {
        singletonInstance = new CECIPropertiesSingleton();
        singletonInstance.activate();
        CECIPropertiesSingleton.setCps(null);
        CECIPropertiesSingleton.setCps(cpsMock);        
        Assert.assertEquals("CECIPropertiesSingleton.cps() should return the mocked cps", cpsMock, CECIPropertiesSingleton.cps());
        singletonInstance.deacivate();
    }
}
