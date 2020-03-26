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
 * Selenium DSE Instance CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.dse.instance.name
 * 
 * @galasa.description Provides a DSE instance for selenium properties
 * 
 * @galasa.required No
 * 
 * @galasa.default PRIMARY
 * 
 * @galasa.valid_values A valid uppercase String
 * 
 * @galasa.examples 
 * <code>selenium.dse.instance.name=PRIMARY</code>
 * 
 */
public class SeleniumDseInstanceName extends CpsProperties {

    public static String get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringWithDefault(SeleniumPropertiesSingleton.cps(), "PRIMARY", "dse", "instance.name");
    }

}