package dev.galasa.elasticlog.internal.properties;

import dev.galasa.elasticlog.internal.ElasticLogManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ElasticLogEndpoint extends CpsProperties {

    public static String get() throws ElasticLogManagerException {
		try {
			String elasticEndpoint = getStringNulled(ElasticLogPropertiesSingleton.cps(), "endpoint", "server.address");

			if (elasticEndpoint == null) {
				throw new ElasticLogManagerException("Could not find a ElasticLog endpoint in CPS.");
			}
			return elasticEndpoint;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ElasticLogManagerException("Problem asking the CPS for the elastic endpoint address", e);
        }
	}
}