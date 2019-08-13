package dev.voras.common.zosbatch.zosmf.internal.properties;

import dev.voras.common.zosbatch.ZosBatchManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * zOS Batch job execution wait timeout
 * <p>
 * The value in milliseconds to wait for the zOS Batch job execution to complete when submitted via zOSMF
 * </p>
 * The property is:<br>
 * {@code zosbatch.batchjob.[imageid].request.retry=60000}
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
