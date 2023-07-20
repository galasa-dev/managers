/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sem.SemManagerException;

public class ModelApplids extends CpsProperties {

    public static @NotNull List<String> get() throws SemManagerException {
        return getStringListWithDefault(SemPropertiesSingleton.cps(), "GAL{0-9}", "model", "applids");
    }
}
