package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

public class SeleniumWebDriver extends CpsProperties {

    public static String get(String instance) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringWithDefault(SeleniumPropertiesSingleton.cps(), "FIREFOX", "instance", "web.driver", instance);
    }

}