/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Selenium Driver Path CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.remote.driver.endpoint
 * 
 * @galasa.description Provides a endpoint for a remote driver. Provisioned if not specified
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A valid String representation of a path
 * 
 * @galasa.examples 
 * <code>selenium.remote.driver.endpoint=http://localhost:4444/wd/hub</code>
 * 
 */
public class SeleniumRemoteDriverEndpoint extends CpsProperties {

    public static String get(String instance, String browser) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringNulled(SeleniumPropertiesSingleton.cps(), "remote",  "endpoint", "driver");
    }

}