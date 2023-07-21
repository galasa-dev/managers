/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.properties;

import dev.galasa.ProductVersion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sem.SemManagerException;

public class InteralVersion extends CpsProperties {

	public static String get(ProductVersion version) throws SemManagerException {
		try {
			String internalVersion =  getStringNulled(SemPropertiesSingleton.cps(), "internal.version", version.toString());
			if (internalVersion == null) {
				throw new SemManagerException("Version '" + version + "' does not have an internal version");
			}

			return internalVersion;
		} catch(ConfigurationPropertyStoreException e) {
			throw new SemManagerException("Problem retrieving internal version from CPS for '" + version.toString() + "'", e);
		}
	}
}
