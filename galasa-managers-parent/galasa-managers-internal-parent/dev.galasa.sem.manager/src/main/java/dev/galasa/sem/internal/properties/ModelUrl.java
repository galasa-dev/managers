/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sem.SemManagerException;

public class ModelUrl extends CpsProperties {

	public static URL get() throws SemManagerException {
		try {
			String url = getStringNulled(SemPropertiesSingleton.cps(), "model", "url");
			if (url == null) {
				throw new SemManagerException("sem.model.url is missing from the CPS");
			}

			try {
				return new URL(url);
			} catch(MalformedURLException e) {
				throw new SemManagerException("Unable to parse the sem.model.url '" + url + "'", e);
			}
		} catch(ConfigurationPropertyStoreException e) {
			throw new SemManagerException("Problem accessing the CPS for the sem.model.url", e);
		}
	}
}
