/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * RSE API Server port
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[imageid].https
 * 
 * @galasa.description The hostname RSE API server 
 * 
 * @galasa.required Yes
 * 
 * @galasa.default 6800
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>rseapi.server.port=6800</code><br>
 * <code>rseapi.server.SYSA.port=6800</code>
 *
 */
public class ServerPort extends CpsProperties {

    public static String get(String imageId) throws RseapiManagerException {
        try {
            String serverPort = getStringNulled(RseapiPropertiesSingleton.cps(), "server", "port", imageId);

            if (serverPort == null) {
                return "6800";
            }
            int serverPortInt = Integer.parseInt(serverPort);
            if (serverPortInt < 0 || serverPortInt > 65535) {
                throw new RseapiManagerException("Invalid value (" + serverPort + ") for RSE API server port property for zOS image "  + imageId + ". Range  0-65535");
            }
            return serverPort;
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem asking the CPS for the RSE API server port property for zOS image "  + imageId, e);
        }
    }

}
