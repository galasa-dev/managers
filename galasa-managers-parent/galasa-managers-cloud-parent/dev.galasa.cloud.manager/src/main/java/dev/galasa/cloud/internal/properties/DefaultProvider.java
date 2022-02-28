/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.cloud.internal.properties;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;


/**
 * Container provider override
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cloud.container.override.TAG.provider
 * 
 * @galasa.description Overrides the provider used for tagged container 
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid provider name, depending on CPS configuration could be,  docker, k8s, gcprun, awsfargate etc
 * 
 * @galasa.examples 
 * <code>cloud.container.override.API.provider=k8s<br>
 * </code>
 * */
public class DefaultProvider extends CpsProperties {

	public static String get(final String tag) throws CloudManagerException {
		try {
			return getStringNulled(CloudPropertiesSingleton.cps(), "cloud.container.override." + tag, "provider");
		} catch (Exception e) {
			throw new CloudManagerException("Problem accessing CPS for cloud container provider override", e);
		}
	}
}
