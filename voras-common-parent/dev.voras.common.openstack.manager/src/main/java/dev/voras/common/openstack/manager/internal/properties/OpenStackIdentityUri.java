package dev.voras.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogFactory;

import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * OpenStack Identity URI
 * <p>
 * The Openstack Identity URI that the OpenStack Manager will authenticate against and retrieve the other endpoints
 * </p><p>
 * The property is:-<br><br>
 * openstack.server.identity.uri=https://openstack.com:9999/identity 
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class OpenStackIdentityUri extends CpsProperties {
	
	public static String get(IConfigurationPropertyStoreService cps) throws ConfigurationPropertyStoreException {
		return getStringNulled(cps, 
				               LogFactory.getLog(OpenStackIdentityUri.class), 
				               "server", 
				               "identity.uri");
	}

}
