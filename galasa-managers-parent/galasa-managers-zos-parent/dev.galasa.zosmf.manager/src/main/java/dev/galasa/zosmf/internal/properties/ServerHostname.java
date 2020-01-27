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
 * zOSMF Server hostname
 * <p>
 * The hostname zOSMF server 
 * </p><p>
 * The property is:<br>
 * {@code zosmf.server.[imageid].hostname=zosmfserver.ibm.com} 
 * </p>
 * <p>
 * There is no default value
 * </p>
 *
 */
public class ServerHostname extends CpsProperties {

    public static String get(String imageId) throws ZosmfManagerException {
        try {
            String serverHostname = getStringNulled(ZosmfPropertiesSingleton.cps(), "server", "hostname", imageId);

            if (serverHostname == null) {
                throw new ZosmfManagerException("Value for zOSMF server hostname not configured for zOS image "  + imageId);
            }
            return serverHostname;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Problem asking the CPS for the zOSMF server hostname for zOS image "  + imageId, e);
        }
    }

}
