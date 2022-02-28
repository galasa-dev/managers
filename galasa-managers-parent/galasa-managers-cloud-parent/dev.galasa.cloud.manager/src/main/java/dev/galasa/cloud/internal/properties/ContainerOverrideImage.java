/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.cloud.internal.properties;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;


/**
 * Container image override
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cloud.container.override.TAG.image
 * 
 * @galasa.description Overrides the image name used for tagged container 
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid image name
 * 
 * @galasa.examples 
 * <code>cloud.container.override.API.image=icr.io/galasadev/demo/api:prod<br>
 * </code>
 * */
public class ContainerOverrideImage extends CpsProperties {

	public static String get(final String tag) throws CloudManagerException {
		try {
			return getStringNulled(CloudPropertiesSingleton.cps(), "cloud.container.override." + tag, "image");
		} catch (Exception e) {
			throw new CloudManagerException("Problem accessing CPS for cloud container image override", e);
		}
	}
}
