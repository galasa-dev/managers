package dev.galasa.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogConfigurationException;

import dev.galasa.common.openstack.manager.OpenstackManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * OpenStack Domain name
 * <p>
 * The Openstack Domain name that the manager will authenticate against and create compute resources under.
 * This property is required as no default is available. 
 * </p><p>
 * The property is:-<br><br>
 * openstack.server.domain.name=xxxxx 
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class OpenStackDomainName extends CpsProperties {
	
	public static String get() throws ConfigurationPropertyStoreException, OpenstackManagerException, LogConfigurationException {
		return getStringNulled(OpenstackPropertiesSingleton.cps(), 
				               "server", 
				               "domain.name");
	}

}
