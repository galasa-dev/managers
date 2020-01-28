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
 * @galasa.description Provides the index in elasticsearch requests are directed to
 * 
 * @galasa.required Yes
 * 
 * @galasa.default galasa
 * 
 * @galasa.valid_values Any lowercase, single word string
 * 
 * @galasa.examples 
 * <code>elastic.endpoint.index=galasa</code>
 * 
 * @galasa.extra
 * The given index will be created and mapped to the galasa run if it does not exist.</br>
 * If the index already exists, it must have the mapping of the given galasa run.
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