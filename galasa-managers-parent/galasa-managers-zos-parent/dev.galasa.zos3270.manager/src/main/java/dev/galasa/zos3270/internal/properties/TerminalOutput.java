/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * The 3270 terminal outputs to store in the RAS.
 * </p>
 * <p>
 * The property is:<br>
 * <br>
 * zos3270.terminal.output=json,png
 * </p>
 * <p>
 * The default is json.
 * </p>
 * 
 *
 */
public class TerminalOutput extends CpsProperties {

    public static List<String> get() throws Zos3270ManagerException {
        return getStringListWithDefault(Zos3270PropertiesSingleton.cps(), "json", "terminal", "output");
    }
}
