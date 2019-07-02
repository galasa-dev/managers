package dev.voras.common.linux.internal.properties;

import dev.voras.common.linux.LinuxManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * Linux SSH Credentials
 * <p>
 * The IPV4 Credentials for connecting via SSH to the DSE server
 * </p><p>
 * The property is:-<br><br>
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
		return getStringNulled(LinuxPropertiesSingleton.cps(), 
				                 "dse.tag." + tag, 
				                 "ssh.credentials");
	}

}
