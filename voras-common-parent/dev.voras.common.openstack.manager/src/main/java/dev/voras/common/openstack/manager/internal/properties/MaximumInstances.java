package dev.voras.common.openstack.manager.internal.properties;

import org.apache.commons.logging.LogFactory;

import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * OpenStack Maximum Compute Instances
 * <p>
 * This property restricts the maximum number of instances the OpenStack Manager 
 * can create across all tests. 
 * </p><p>
 * The property is:-<br><br>
 * openstack.server.maximum.compute.instances=9 
 * </p>
 * <p>
 * default value is 2 instaces
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class MaximumInstances extends CpsProperties {
	
	public static int get(IConfigurationPropertyStoreService cps) {
		return getIntWithDefault(cps, 
				                 LogFactory.getLog(MaximumInstances.class), 
				                 2, 
				                 "server", 
				                 "maximum.compute.instances");
	}

}
