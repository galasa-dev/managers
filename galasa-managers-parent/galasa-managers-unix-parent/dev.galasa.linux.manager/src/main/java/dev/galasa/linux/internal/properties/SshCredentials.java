/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.linux.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.LinuxManagerException;

/**
 * Linux SSH Credentials
 * <p>
 * The IPV4 Credentials for connecting via SSH to the DSE server
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * linux.dse.tag.[tag].ssh.credentials=sshcreds
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class SshCredentials extends CpsProperties {

    public static String get(String tag) throws LinuxManagerException, ConfigurationPropertyStoreException {
        return getStringNulled(LinuxPropertiesSingleton.cps(), "dse.tag." + tag, "ssh.credentials");
    }

}
