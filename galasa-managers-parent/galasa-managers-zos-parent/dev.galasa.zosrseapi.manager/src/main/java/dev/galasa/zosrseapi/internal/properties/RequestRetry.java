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
 * RSE API Server retry request
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[imageid].https
 * 
 * @galasa.description The number of times to retry when RSE API request fails
 * 
 * @galasa.required No
 * 
 * @galasa.default 3
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>rseapi.server.request.retry=5</code><br>
 * <code>rseapi.server.SYSA.request.retry=5</code>
 *
 */
public class RequestRetry extends CpsProperties {

    private static final int DEFAULT_REQUEST_RETRY = 3;

    public static int get(String imageId) throws RseapiManagerException {
        try {
            String retryString = getStringNulled(RseapiPropertiesSingleton.cps(), "command", "request.retry", imageId);

            if (retryString == null) {
                return DEFAULT_REQUEST_RETRY;
            } else {
                return Integer.parseInt(retryString);
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem asking the CPS for the RSE API request retry property for zOS image "  + imageId, e);
        }
    }

}
