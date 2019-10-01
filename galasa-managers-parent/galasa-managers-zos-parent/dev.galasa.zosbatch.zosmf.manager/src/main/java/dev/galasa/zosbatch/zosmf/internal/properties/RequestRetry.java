package dev.galasa.zosbatch.zosmf.internal.properties;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOS Batch job retry request
 * <p>
 * The number of times to retry zOS Batch job request when zOSMF request fails
 * </p>
 * The property is:<br>
 * {@code zosbatch.batchjob.[imageid].request.retry=5}
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_REQUEST_RETRY}
 * </p>
 *
 */
public class RequestRetry extends CpsProperties {

	private static final int DEFAULT_REQUEST_RETRY = 3;

	public static int get(String imageId) throws ZosBatchManagerException {
		try {
			String retryString = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "batchjob", "request.retry", imageId);

			if (retryString == null) {
				return DEFAULT_REQUEST_RETRY;
			} else {
				return Integer.parseInt(retryString);
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosBatchManagerException("Problem asking the CPS for the batch job request retry property for zOS image "  + imageId, e);
		}
	}

}
