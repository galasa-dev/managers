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
 * @galasa.name selenium.instance.INSTANCE.web.driver
 * 
 * @galasa.description Provides the browser of the webdriver needed for a given instance
 * 
 * @galasa.required Yes
 * 
 * @galasa.valid_values GECKO,IE,EDGE,CHROME
 * 
 * @galasa.examples 
 * <code>selenium.instance.PRIMARY.web.driver=GECKO</code>
 * 
 */
public class SeleniumWebDriver extends CpsProperties {

    public static String get(String instance) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringWithDefault(SeleniumPropertiesSingleton.cps(), "GECKO", "instance", "web.driver", instance);
    }

}