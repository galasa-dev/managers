/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Server port is https
 * <p>
 * Use https (SSL) for zOSMF server 
 * </p><p>
 * The property is:<br>
 * {@code zosmf.server.[imageid].https=true} 
 * </p>
 * <p>
 * The default value is {@value #USE_HTTPS}
 * </p>
 *
 */
public class Https extends CpsProperties {
	
	private static final boolean USE_HTTPS = true;
	
	public static boolean get(String imageId) throws ZosmfManagerException {
		try {
			String https = getStringNulled(ZosmfPropertiesSingleton.cps(), "server", "https", imageId);

			if (https == null) {
				return USE_HTTPS;
			}
			return Boolean.valueOf(https);
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosmfManagerException("Problem asking the CPS for the zOSMF server use https for zOS image "  + imageId, e);
		}
	}

}
