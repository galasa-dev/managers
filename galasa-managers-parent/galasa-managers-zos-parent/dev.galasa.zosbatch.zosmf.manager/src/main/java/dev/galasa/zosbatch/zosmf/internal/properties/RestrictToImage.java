package dev.galasa.zosbatch.zosmf.internal.properties;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Restrict processing to the zOSMF server on the specified image
 * <p>
 * Use only the zOSMF server running on the image associated with the zOS Batch job
 * </p><p>
 * The property is:<br>
 * {@code zosbatch.batchjob.[imageid].restrict.to.image=true}
 * </p>
 * <p>
 * The default value is {@value #RESTRICT_TO_IMAGE}
 * </p>
 *
 */
public class RestrictToImage extends CpsProperties {

	private static final boolean RESTRICT_TO_IMAGE = false;

	public static boolean get(String imageId) throws ZosBatchManagerException {
		try {
			String sysaffString = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "batchjob", "restrict.to.image", imageId);

			if (sysaffString == null) {
				return RESTRICT_TO_IMAGE;
			} else {
				return Boolean.parseBoolean(sysaffString);
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosBatchManagerException("Problem asking the CPS for the batch job restrict to immage property for zOS image "  + imageId, e);
		}
	}

}
