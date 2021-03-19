/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Selenium Driver Type CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.driver.type
 * 
 * @galasa.description Describes the selenium runtime that will be used.
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A valid String representation of a type. Available choices: local, docker, kubernetes, grid
 * 
 * @galasa.examples 
 * <code>selenium.driver.type=docker</code>
 * 
 */
public class SeleniumAvailableDrivers extends CpsProperties {
    
    public static String[] get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        String drivers = getStringNulled(SeleniumPropertiesSingleton.cps(), "available", "drivers");
        if (drivers != null) {
            return drivers.split(",");
        }
        throw new SeleniumManagerException("No avilable drivers, set selenium.available.drivers.");
    }

}