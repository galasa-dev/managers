package dev.galasa.elastic.internal.properties;

import dev.galasa.elastic.ElasticManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ElasticEndpoint extends CpsProperties {

    public static String get() throws ElasticManagerException {
		try {
			String elasticEndpoint = getStringNulled(ElasticPropertiesSingleton.cps(), "endpoint", "server.address");

			if (elasticEndpoint == null) {
				throw new ElasticManagerException("Could not find a docker server in CPS.");
			}
			return elasticEndpoint;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ElasticManagerException("Problem asking the CPS for the elastic endpoint address", e);
        }
	}
}