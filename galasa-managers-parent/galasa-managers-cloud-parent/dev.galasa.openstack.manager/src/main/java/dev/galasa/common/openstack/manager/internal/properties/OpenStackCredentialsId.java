package dev.galasa.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogConfigurationException;

import dev.galasa.common.openstack.manager.OpenstackManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

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
	
	public static String get() throws OpenstackManagerException, LogConfigurationException {
		return getStringWithDefault(OpenstackPropertiesSingleton.cps(), 
				               "openstack",
				               "server", 
				               "credentials.id");
	}

}
