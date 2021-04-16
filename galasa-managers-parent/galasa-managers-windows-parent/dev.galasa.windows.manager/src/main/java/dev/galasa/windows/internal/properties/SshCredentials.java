/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.windows.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.windows.WindowsManagerException;

/**
 * Windows SSH Credentials
 * <p>
 * The IPV4 Credentials for connecting via SSH to the DSE server
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * windows.dse.tag.[tag].ssh.credentials=sshcreds
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class SshCredentials extends CpsProperties {

    public static String get(String tag) throws WindowsManagerException, ConfigurationPropertyStoreException {
        return getStringNulled(WindowsPropertiesSingleton.cps(), "dse.tag." + tag, "ssh.credentials");
    }

}
