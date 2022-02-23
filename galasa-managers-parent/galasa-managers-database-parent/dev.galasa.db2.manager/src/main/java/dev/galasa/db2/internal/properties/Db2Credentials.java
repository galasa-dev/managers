package dev.galasa.db2.internal.properties;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class Db2Credentials extends CpsProperties {

    public static String get(String tag) throws Db2ManagerException {
		try {

			final String credentialsKey = getStringNulled(Db2PropertiesSingleton.cps(), "instance", "credentials", tag);

			if (credentialsKey == null) {
				throw new Db2ManagerException("Could not find credentials for db2 instance: " + tag);
			}
			return credentialsKey;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new Db2ManagerException("Problem asking the CPS for properties", e);
        }
	}
}