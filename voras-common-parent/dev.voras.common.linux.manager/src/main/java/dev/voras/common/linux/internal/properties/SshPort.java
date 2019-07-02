package dev.voras.common.linux.internal.properties;

import dev.voras.common.linux.LinuxManagerException;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * Linux SSH Port
 * <p>
 * The SSH Port for the Linux DSE server
 * </p><p>
 * The property is:-<br><br>
 * linux.dse.tag.[tag].ssh.port=9 
 * </p>
 * <p>
 * default value is 22
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class SshPort extends CpsProperties {
	
	public static int get(String tag) throws LinuxManagerException {
		return getIntWithDefault(LinuxPropertiesSingleton.cps(), 
				                 22, 
				                 "dse.tag." + tag, 
				                 "ssh.port");
	}

}
