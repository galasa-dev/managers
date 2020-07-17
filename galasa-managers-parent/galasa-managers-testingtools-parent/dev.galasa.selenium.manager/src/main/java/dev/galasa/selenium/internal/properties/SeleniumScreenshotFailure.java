package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

/**
* Selenium Gecko Profile CPS Property
* 
* @galasa.cps.property
* 
* @galasa.name selenium.instance.INSTANCE.gecko.profile
* 
* @galasa.description Provides a profile to use when using the gecko driver for extensions
* 
* @galasa.required No
* 
* @galasa.valid_values A valid String name of a profile
* 
* @galasa.examples 
* <code>selenium.instance.PRIMARY.gecko.profile=default</code>
* 
*/
public class SeleniumScreenshotFailure extends CpsProperties {

    public static Boolean get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return Boolean.valueOf(getStringWithDefault(SeleniumPropertiesSingleton.cps(), "false", "screenshot", "failure"));
    }

}