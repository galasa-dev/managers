/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.linux.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.LinuxManagerException;

/**
 * Linux Capabilities
 * <p>
 * What the capabilities are of the linux image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * linux.image.GALLNX.capabilities=desktop,wmq
 * linux.image.capabilities=desktop,wmq
 * </p>
 * <p>
 * The default is empty, ie no special capabilities
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class LinuxCapabilities extends CpsProperties {

    public static List<String> get(String imageName) throws ConfigurationPropertyStoreException, LinuxManagerException {
        return getStringList(LinuxPropertiesSingleton.cps(), "image", "capabilities", imageName);
    }

}
