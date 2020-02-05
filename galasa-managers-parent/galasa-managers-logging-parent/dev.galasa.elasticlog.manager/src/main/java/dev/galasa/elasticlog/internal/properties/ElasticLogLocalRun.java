/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.elasticlog.internal.properties;

import dev.galasa.elasticlog.internal.ElasticLogManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * ElasticLog Endpoint Index CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name elastic.endpoint.index
 * 
 * @galasa.description Provides the index in elasticsearch to which requests are directed 
 * 
 * @galasa.required Yes
 * 
 * @galasa.default galasa
 * 
 * @galasa.valid_values Any lowercase, single-word string
 * 
 * @galasa.examples 
 * <code>elastic.endpoint.index=galasa</code>
 * 
 * @galasa.extra
 * If the index does not exist, the index is created and is mapped to the Galasa run.</br>
 * If the index exists, it must be mapped to the relevant Galasa run.
 * 
 */
public class ElasticLogLocalRun extends CpsProperties {

    public static String get() throws ElasticLogManagerException {
		try {
			String elasticLogLocalRun = getStringNulled(ElasticLogPropertiesSingleton.cps(), "local", "run.log");

			return elasticLogLocalRun;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ElasticLogManagerException("Problem asking the CPS for the ElasticLog Local Run", e);
        }
	}
}