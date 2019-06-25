package dev.voras.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogFactory;

import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * OpenStack project name
 * <p>
 * The Openstack Project name that the manager will authenticate against and create compute resources under.
 * This property is required as no default is available. 
 * </p><p>
 * The property is:-<br><br>
 * openstack.server.project.name=xxxxx 
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class OpenStackProjectName extends CpsProperties {
	
	public static String get(IConfigurationPropertyStoreService cps) throws ConfigurationPropertyStoreException {
		return getStringNulled(cps, 
				               LogFactory.getLog(OpenStackProjectName.class), 
				               "server", 
				               "project.name");
	}

}
