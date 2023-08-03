/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.properties;

import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sem.SemManagerException;

public class ExternalVersion extends CpsProperties {

	public static ProductVersion get(String version) throws SemManagerException {
		try {
			String internalVersion =  getStringNulled(SemPropertiesSingleton.cps(), "external.version", version);
			if (internalVersion == null) {
				throw new SemManagerException("Version '" + version + "' does not have an external version");
			}

			try {
				return ProductVersion.parse(internalVersion);
			} catch(ManagerException e) {
				throw new SemManagerException("Internal version was not recognised '" + version + "'", e);
			}
		} catch(ConfigurationPropertyStoreException e) {
			throw new SemManagerException("Problem retrieving external version from CPS for '" + version + "'", e);
		}
	}
}
