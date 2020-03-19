package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

public class SeleniumWebDriverPath extends CpsProperties {

    public static String get(String instance, String browser) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringNulled(SeleniumPropertiesSingleton.cps(), "instance",  "path", instance, browser);
    }

}