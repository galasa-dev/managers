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
 * Selenium Driver Path CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.local.driver.BROWSER.path
 * 
 * @galasa.description Provides a path to the webdriver on the system being tested
 * 
 * @galasa.required Yes
 * 
 * @galasa.valid_values A valid String representation of a path
 * 
 * @galasa.examples 
 * <code>selenium.local.driver.CHROME.path=/usr/bin/chromedriver</code>
 * 
 */
public class SeleniumLocalDriverPath extends CpsProperties {

    public static String get(String browser) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringNulled(SeleniumPropertiesSingleton.cps(), "local",  "path", "driver", browser);
    }

}