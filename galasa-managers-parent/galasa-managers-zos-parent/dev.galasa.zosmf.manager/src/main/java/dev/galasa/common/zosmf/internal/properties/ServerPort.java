package dev.galasa.common.zosmf.internal.properties;

import dev.galasa.common.zosmf.ZosmfManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOSMF Server port
 * <p>
 * The hostname zOSMF server 
 * </p><p>
 * The property is:<br>
 * {@code zosmf.server.[imageid].port=443} 
 * </p>
 * <p>
 * There is no default value
 * </p>
 *
 */
public class ServerPort extends CpsProperties {

	public static String get(String imageId) throws ZosmfManagerException {
		try {
			String serverPort = getStringNulled(ZosmfPropertiesSingleton.cps(), "server", "port", imageId);

			if (serverPort == null) {
				throw new ZosmfManagerException("Value for zOSMF server port not configured for zOS image "  + imageId);
			}
			return serverPort;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosmfManagerException("Problem asking the CPS for the zOSMF server port for zOS image "  + imageId, e);
		}
	}

}
