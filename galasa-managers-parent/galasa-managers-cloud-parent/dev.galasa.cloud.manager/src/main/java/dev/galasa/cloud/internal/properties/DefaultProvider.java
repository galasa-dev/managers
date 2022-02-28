/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.cloud.internal.properties;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Default provider of cloud containers
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cloud.default.provider
 * 
 * @galasa.description Specifies the default provider of cloud containers
 * 
 * @galasa.required No
 * 
 * @galasa.default docker
 * 
 * @galasa.valid_values A valid provider name, depending on CPS configuration could be, docker, k8s, gcprun, awsfargate etc
 * 
 * @galasa.examples 
 * <code>cloud.default.provider=k8s<br>
 * </code>
 * */
public class DefaultProvider extends CpsProperties {

	public static String get() throws CloudManagerException {
		try {
			return getStringNulled(CloudPropertiesSingleton.cps(), "default", "provider");
		} catch (Exception e) {
			throw new CloudManagerException("Problem accessing CPS for default provider", e);
		}
	}
}
