package dev.galasa.elasticlog.internal.properties;

import dev.galasa.elasticlog.internal.ElasticLogManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ElasticLogEndpoint extends CpsProperties {

    public static String get() throws ElasticLogManagerException {
		try {
			String elasticLogEndpoint = getStringNulled(ElasticLogPropertiesSingleton.cps(), "endpoint", "address");

			if (elasticLogEndpoint == null) {
				throw new ElasticLogManagerException("Could not find a ElasticLog endpoint in CPS.");
			}
			return elasticLogEndpoint;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ElasticLogManagerException("Problem asking the CPS for the ElasticLog Endpoint", e);
        }
	}
}