/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile.zosmf.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosfile.ZosFileManagerException;

/**
 * UNIX permission bits to be used in creating the file or directory
 * <p>
 * The UNIX file or directory permission bits to be used in creating the file or directory
 * </p><p>
 * The property is:<br>
 * {@code zosfile.[imageid].unix.file.permission=rwxrwx---}
 * </p>
 * <p>
 * The default value is {@value #UNIX_FILE_PERMISSIONS}
 * </p>
 *
 */
public class UnixFilePermissions extends CpsProperties {

	private static final String UNIX_FILE_PERMISSIONS = "rwxrwxr-x";

	public static String get(String imageId) throws ZosFileManagerException {
		try {
			String modeString = getStringNulled(ZosFileZosmfPropertiesSingleton.cps(), "zosfile","unix.file.permissions", imageId);

			if (modeString == null) {
				return UNIX_FILE_PERMISSIONS;
			} else {
				return modeString;
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosFileManagerException("Problem asking the CPS for the default file permissions property for zOS image "  + imageId, e);
		}
	}

}
