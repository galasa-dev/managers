/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2.internal.properties;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * DB2 DSE instance name
 * 
 * @galasa.cps.property
 * 
 * @galasa.name db2.dse.instance.PRIMARY
 * 
 * @galasa.description Url description including port for the database
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid Db2 Url
 * 
 * @galasa.examples 
 * <code>db2.dse.instance.PRIMARY=db2://example.url.com:40100/DATABASE<br>
 * </code>
 * */
public class Db2DSEInstance extends CpsProperties {

    public static String get(String tag) throws Db2ManagerException {
		try {

			final String instance = getStringNulled(Db2PropertiesSingleton.cps(), "dse.instance", tag);

			if (instance == null) {
				throw new Db2ManagerException("Could not DSE Db2 Instance url with Tag: " + tag);
			}
			return instance;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new Db2ManagerException("Problem asking the CPS for properties", e);
        }
	}
}
