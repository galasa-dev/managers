/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sem.SemManagerException;

public class CicsBuild extends CpsProperties {

    public static String get() throws SemManagerException {
        try {
            return getStringNulled(SemPropertiesSingleton.cps(), "build", "level");
        } catch (ConfigurationPropertyStoreException e) {
            throw new SemManagerException("Problem accessing sem.build.level", e);
        }
    }
}
