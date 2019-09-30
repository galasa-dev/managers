package dev.galasa.common.zosbatch.zosmf.internal.properties;

import dev.galasa.common.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOS Batch job use SYSAFF
 * <p>
 * Use the run the zOS Batch job on the specified image by specifying<br>
 * {@code /*JOBPARM SYSAFF=[imageid]}
 * </p><p>
 * The property is:<br>
 * {@code zosbatch.batchjob.[imageid].use.sysaff=true}
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_USE_SYSAFF}
 * </p>
 *
 */
public class UseSysaff extends CpsProperties {

	private static final boolean DEFAULT_USE_SYSAFF = true;

	public static boolean get(String imageId) throws ZosBatchManagerException {
		try {
			String sysaffString = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "batchjob", "use.sysaff", imageId);

			if (sysaffString == null) {
				return DEFAULT_USE_SYSAFF;
			} else {
				return Boolean.parseBoolean(sysaffString);
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosBatchManagerException("Problem asking the CPS for the batch job use SYSAFF property for zOS image "  + imageId, e);
		}
	}

}
