package dev.voras.common.linux.internal.properties;

import dev.voras.common.linux.LinuxManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * Linux Hostname IPV6
 * <p>
 * The IPV6 hostname of the Linux DSE server
 * </p><p>
 * The property is:-<br><br>
 * linux.dse.tag.[tag].hostname6=cics.ibm.com 
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class Hostname6 extends CpsProperties {
	
	public static String get(String tag) throws LinuxManagerException, ConfigurationPropertyStoreException {
		return getStringNulled(LinuxPropertiesSingleton.cps(), 
				                 "dse.tag." + tag, 
				                 "hostname6");
	}

}
