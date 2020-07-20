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
 * Selenium Gecko Preferences CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.instance.INSTANCE.gecko.preferences
 * 
 * @galasa.description Provides extra preferences to use when using the gecko driver for extensions
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A comma seperated list of key value pairs for the preferences
 * 
 * @galasa.examples 
 * <code>selenium.instance.PRIMARY.gecko.preferences=app.update.silent=false,dom.popup_maximum=0</code>
 * 
 */
public class SeleniumGeckoPreferences extends CpsProperties {

    public static String get(String instance) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringNulled(SeleniumPropertiesSingleton.cps(), "instance", "gecko.preferences", instance);
    }

}