/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf.internal.properties;

import java.util.Arrays;
import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Server images
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.[clusterid].images
 * 
 * @galasa.description The zOSMF server images active on the supplied cluster 
 * 
 * @galasa.required No
 * 
 * @galasa.default True
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosmf.server.images=SYSA,SYSB</code><br>
 * <code>zosmf.server.PLEXA.images=SYSA,SYSB</code>
 *
 */
public class ServerImages extends CpsProperties {

    public static List<String> get(String clusterId) throws ZosmfManagerException {
        try {
            String serverImages = getStringNulled(ZosmfPropertiesSingleton.cps(), "server", "images", clusterId);

            if (serverImages == null) {
                throw new ZosmfManagerException("Value for zOSMF server images property not configured for zOS cluster "  + clusterId);
            }
            return Arrays.asList(serverImages.split(","));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Problem asking the CPS for the zOSMF server images property for zOS cluster "  + clusterId, e);
        }
    }

}
