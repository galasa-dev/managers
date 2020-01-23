package dev.galasa.elasticlog.internal.properties;

import dev.galasa.elasticlog.internal.ElasticLogManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ElasticLogIndex extends CpsProperties {

    public static String get() throws ElasticLogManagerException {
		try {
			String elasticLogIndex = getStringNulled(ElasticLogPropertiesSingleton.cps(), "endpoint", "index");

			if (elasticLogIndex == null) {
				throw new ElasticLogManagerException("Could not find a ElasticLog index in CPS.");
			}
			return elasticLogIndex;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ElasticLogManagerException("Problem asking the CPS for the ElasticLog Index", e);
        }
	}
}