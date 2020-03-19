package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

public class SeleniumGeckoProfile extends CpsProperties {

    public static String get(String instance) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringNulled(SeleniumPropertiesSingleton.cps(), "instance", "gecko.profile", instance);
    }

}