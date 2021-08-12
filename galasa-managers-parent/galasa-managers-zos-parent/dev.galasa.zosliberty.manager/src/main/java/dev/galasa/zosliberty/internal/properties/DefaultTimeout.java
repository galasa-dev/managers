/*
 /*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zosliberty.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosliberty.ZosLibertyManagerException;

/**
 * The Default Timeout value in milliseconds for Liberty servers on a zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosliberty.server.[image].default.timeout
 * 
 * @galasa.description Provides a value for the default timeout for Liberty servers on a zOS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default 20000 milliseconds
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosliberty.server.[image].default.timeout=20000</code><br>
 *
 */
public class DefaultTimeout extends CpsProperties {

    public static int get(IZosImage image) throws ZosLibertyManagerException {
            return getIntWithDefault(ZosLibertyPropertiesSingleton.cps(), 20000, "server", "default.timeout", image.getImageID());
    }
}
