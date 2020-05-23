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
 * <code>zosmf.server.hostname=zosmfserver.ibm.com</code><br>
 * <code>zosmf.server.SYSA.hostname=zosmfserver.ibm.com</code>
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
            throw new ZosmfManagerException("Problem asking the CPS for the zOSMF server hostname property for zOS image "  + imageId, e);
        }
    }

}
