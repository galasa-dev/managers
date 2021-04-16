/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.windows.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.windows.WindowsManagerException;

/**
 * Windows SSH Port
 * <p>
 * The SSH Port for the Windows DSE server
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * windows.dse.tag.[tag].ssh.port=9
 * </p>
 * <p>
 * default value is 22
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class SshPort extends CpsProperties {

    public static int get(String tag) throws WindowsManagerException {
        return getIntWithDefault(WindowsPropertiesSingleton.cps(), 22, "dse.tag." + tag, "ssh.port");
    }

}
