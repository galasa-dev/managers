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
 * RSE API Server hostname
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[imageid].https
 * 
 * @galasa.description The hostname RSE API server
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>rseapi.server.hostname=rseapiserver.ibm.com</code><br>
 * <code>rseapi.server.SYSA.hostname=rseapiserver.ibm.com</code>
 *
 */
public class ServerHostname extends CpsProperties {

    public static String get(String imageId) throws RseapiManagerException {
        try {
            String serverHostname = getStringNulled(RseapiPropertiesSingleton.cps(), "server", "hostname", imageId);

            if (serverHostname == null) {
                throw new RseapiManagerException("Value for RSE API server hostname not configured for zOS image "  + imageId);
            }
            return serverHostname;
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem asking the CPS for the RSE API server hostname property for zOS image "  + imageId, e);
        }
    }

}
