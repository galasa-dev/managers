package dev.galasa.openstack.manager.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

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
	
	public static int get() throws OpenstackManagerException {
		return getIntWithDefault(OpenstackPropertiesSingleton.cps(), 
				                 5, 
				                 "timeout", 
				                 "generate");
	}

}
