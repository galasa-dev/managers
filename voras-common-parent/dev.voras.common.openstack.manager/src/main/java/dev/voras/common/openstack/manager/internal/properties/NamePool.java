package dev.voras.common.openstack.manager.internal.properties;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import dev.voras.Constants;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * OpenStack Compute Server Name Pool
 * <p>
 * This property indicates what names will be given to the compute servers when they are created. 
 * </p><p>
 * The property is:-<br><br>
 * openstack.server.name.pool=VORAS{0-9}{0-9}
 * </p><p>
 * Can be a comma separated list of static or generated names, eg BOB1,BOB9,BOB5
 * </p><p>
 * default value is VORAS{0-9}{0-9}
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class NamePool extends CpsProperties {
	
	public static List<String> get(IConfigurationPropertyStoreService cps) {
		return getStringListWithDefault(cps, 
				               LogFactory.getLog(NamePool.class), 
				               Constants.LITERAL_NAME + "{0-9}{0-9}",
				               "server", 
				               "name.pool");
	}

}
