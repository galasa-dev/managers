package dev.voras.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogFactory;

import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * OpenStack Credentials ID
 * <p>
 * The Credentials ID to be used to authenticate with the OpenStack Server. 
 * </p><p>
 * The property is:-<br><br>
 * openstack.server.credentials.id=openstack 
 * </p>
 * <p>
 * default value is openstack
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class OpenStackCredentialsId extends CpsProperties {
	
	public static String get(IConfigurationPropertyStoreService cps) {
		return getStringWithDefault(cps, 
				               LogFactory.getLog(OpenStackCredentialsId.class), 
				               "openstack",
				               "server", 
				               "credentials.id");
	}

}
