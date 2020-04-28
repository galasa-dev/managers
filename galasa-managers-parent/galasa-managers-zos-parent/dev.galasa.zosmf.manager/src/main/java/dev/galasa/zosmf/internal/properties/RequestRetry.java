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
 * zOSMF Server retry request
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.[imageid].https
 * 
 * @galasa.description The number of times to retry when zOSMF request fails
 * 
 * @galasa.required No
 * 
 * @galasa.default 3
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosmf.server.request.retry=5</code><br>
 * <code>zosmf.server.SYSA.request.retry=5</code>
 *
 */
public class RequestRetry extends CpsProperties {

    private static final int DEFAULT_REQUEST_RETRY = 3;

    public static int get(String imageId) throws ZosmfManagerException {
        try {
            String retryString = getStringNulled(ZosmfPropertiesSingleton.cps(), "command", "request.retry", imageId);

            if (retryString == null) {
                return DEFAULT_REQUEST_RETRY;
            } else {
                return Integer.parseInt(retryString);
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Problem asking the CPS for the console command request retry property for zOS image "  + imageId, e);
        }
    }

}
