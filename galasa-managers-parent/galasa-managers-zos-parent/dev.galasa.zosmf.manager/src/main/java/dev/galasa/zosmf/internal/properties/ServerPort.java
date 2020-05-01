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
 * zOSMF Server port
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.[imageid].https
 * 
 * @galasa.description The hostname zOSMF server 
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosmf.server.port=443</code><br>
 * <code>zosmf.server.SYSA.port=443</code>
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
