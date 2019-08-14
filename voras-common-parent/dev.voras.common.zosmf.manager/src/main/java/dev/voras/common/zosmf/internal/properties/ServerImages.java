package dev.voras.common.zosmf.internal.properties;

import java.util.Arrays;
import java.util.List;

import dev.voras.common.zosmf.ZosmfManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * zOSMF Server images
 * <p>
 * The zOSMF server images active on the supplied cluster 
 * </p><p>
 * The property is:<br>
 * {@code zosmf.server.[clusterid].images=SYSA,SYSB} 
 * </p>
 * <p>
 * There is no default value
 * </p>
 *
 */
public class ServerImages extends CpsProperties {

	public static List<String> get(String clusterId) throws ZosmfManagerException {
		try {
			String serverImages = getStringNulled(ZosmfPropertiesSingleton.cps(), "server", "images", clusterId);

			if (serverImages == null) {
				throw new ZosmfManagerException("Value for zOSMF server port not configured for zOS cluster "  + clusterId);
			}
			return Arrays.asList(serverImages.split(","));
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosmfManagerException("Problem asking the CPS for the zOSMF server port for zOS cluster "  + clusterId, e);
		}
	}

}
