package dev.galasa.zosfile.zosmf.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosfile.ZosFileManagerException;

/**
 * Restrict processing to the zOSMF server on the specified image
 * <p>
 * Use only the zOSMF server running on the image associated with the zOS data set or file
 * </p><p>
 * The property is:<br>
 * {@code zosfile.file.[imageid].restrict.to.image=true}
 * </p>
 * <p>
 * The default value is {@value #RESTRICT_TO_IMAGE}
 * </p>
 *
 */
public class RestrictToImage extends CpsProperties {

	private static final boolean RESTRICT_TO_IMAGE = false;

	public static boolean get(String imageId) throws ZosFileManagerException {
		try {
			String sysaffString = getStringNulled(ZosFileZosmfPropertiesSingleton.cps(), "zosfile", "restrict.to.image", imageId);

			if (sysaffString == null) {
				return RESTRICT_TO_IMAGE;
			} else {
				return Boolean.parseBoolean(sysaffString);
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosFileManagerException("Problem asking the CPS for the file restrict to image property for zOS image "  + imageId, e);
		}
	}

}
