package dev.voras.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogFactory;

import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * OpenStack Generate Timeout value 
 * <p>
 * In minutes, how long the OpenStack Manager should wait for 
 * Compute to build and start the server.
 * </p><p>
 * The property is:-<br><br>
 * openstack.timeout.generate=9 
 * </p>
 * <p>
 * default value is 5 minutes
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class GenerateTimeout extends CpsProperties {
	
	public static int get(IConfigurationPropertyStoreService cps) {
		return getIntWithDefault(cps, 
				                 LogFactory.getLog(GenerateTimeout.class), 
				                 5, 
				                 "timeout", 
				                 "generate");
	}

}
