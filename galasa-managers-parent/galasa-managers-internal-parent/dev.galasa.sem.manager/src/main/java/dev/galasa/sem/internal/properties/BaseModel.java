/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sem.SemManagerException;

public class BaseModel extends CpsProperties {

    public static String get() throws SemManagerException {
        return getStringWithDefault(SemPropertiesSingleton.cps(), "Base", "base", "model");
    }
}
