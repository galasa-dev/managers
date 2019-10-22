/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * Which directory to place live terminal updates for displaying in Eclipse
 * <p>
 * Eclipse will set this property in the overrides to indicate the zOS3270 is to 
 * place the terminal images ready for live viewing in the Eclipse UI
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * zos3270.live.terminal.images=xxxxxxxx
 * </p>
 * <p>
 * there is no default,  lack of presence or empty value means no live recording to be done
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class LiveTerminalDirectory extends CpsProperties {

    public static String get() throws Zos3270ManagerException {
        try {
            return getStringNulled(Zos3270PropertiesSingleton.cps(), "live.terminal", "images");
        } catch (ConfigurationPropertyStoreException e) {
            throw new Zos3270ManagerException("Unable to retrieve the live.terminal.images property", e);
        }
    }

}
