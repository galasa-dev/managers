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

public class ModelPorts extends CpsProperties {

    public static @NotNull List<String> get() throws SemManagerException {
        return getStringListWithDefault(SemPropertiesSingleton.cps(), "30000-30010", "model", "ports");
    }
}
