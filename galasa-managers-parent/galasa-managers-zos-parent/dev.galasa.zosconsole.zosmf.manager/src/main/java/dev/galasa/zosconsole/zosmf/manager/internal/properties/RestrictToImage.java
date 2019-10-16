package dev.galasa.zosconsole.zosmf.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosconsole.ZosConsoleManagerException;

/**
 * Restrict zOS console processing to the zOSMF server on the specified image
 * <p>
 * Use only the zOSMF server running on the image associated with the zOS Console
 * </p><p>
 * The property is:<br>
 * {@code zosconsole.console.[imageid].restrict.to.image=true}
 * </p>
 * <p>
 * The default value is {@value #RESTRICT_TO_IMAGE}
 * </p>
 *
 */
public class RestrictToImage extends CpsProperties {

	private static final boolean RESTRICT_TO_IMAGE = false;

	public static boolean get(String imageId) throws ZosConsoleManagerException {
		try {
			String sysaffString = getStringNulled(ZosConsoleZosmfPropertiesSingleton.cps(), "zosconsole", "restrict.to.image", imageId);

			if (sysaffString == null) {
				return RESTRICT_TO_IMAGE;
			} else {
				return Boolean.parseBoolean(sysaffString);
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosConsoleManagerException("Problem asking the CPS for the console restrict to image property for zOS image "  + imageId, e);
		}
	}

}
