/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal.properties;

import java.util.Arrays;
import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * RSE API Server images
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[clusterid].images
 * 
 * @galasa.description The RSE API server images active on the supplied cluster 
 * 
 * @galasa.required No
 * 
 * @galasa.default True
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>rseapi.server.images=SYSA,SYSB</code><br>
 * <code>rseapi.server.PLEXA.images=SYSA,SYSB</code>
 *
 */
public class ServerImages extends CpsProperties {

    public static List<String> get(String clusterId) throws RseapiManagerException {
        try {
            String serverImages = getStringNulled(RseapiPropertiesSingleton.cps(), "server", "images", clusterId);

            if (serverImages == null) {
                throw new RseapiManagerException("Value for RSE API server images property not configured for zOS cluster "  + clusterId);
            }
            return Arrays.asList(serverImages.split(","));
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem asking the CPS for the RSE API server images property for zOS cluster "  + clusterId, e);
        }
    }

}
