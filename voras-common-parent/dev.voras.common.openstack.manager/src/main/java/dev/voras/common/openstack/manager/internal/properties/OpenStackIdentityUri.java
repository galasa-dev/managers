package dev.voras.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogConfigurationException;

import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
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
	
	public static String get() throws ConfigurationPropertyStoreException, OpenstackManagerException, LogConfigurationException {
		return getStringNulled(OpenstackPropertiesSingleton.cps(), 
				               "server", 
				               "identity.uri");
	}

}
