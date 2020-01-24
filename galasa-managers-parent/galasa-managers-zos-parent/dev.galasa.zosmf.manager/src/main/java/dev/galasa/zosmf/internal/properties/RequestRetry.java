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
 * zOS console retry request
 * <p>
 * The number of times to retry zOS console request when zOSMF request fails
 * </p>
 * The property is:<br>
 * {@code zosmf.server.[imageid].request.retry=5}
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_REQUEST_RETRY}
 * </p>
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
