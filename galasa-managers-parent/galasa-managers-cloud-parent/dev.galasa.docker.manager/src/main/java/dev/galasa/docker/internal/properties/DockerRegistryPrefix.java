/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerRegistryImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Registry Prefix CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.registry.ID.prefix
 * 
 * @galasa.description Provides the prefix of a Docker Registry that is used by the Docker Manager.
 * 
 * @galasa.required Yes if the Registry ID is used in the CPS Property <code>docker.default.registries</code>.
 * 
 * @galasa.default None, except for DOCKERHUB where the default is <code>https://registry.hub.docker.com</code>
 * 
 * @galasa.valid_values A valid String
 * 
 * @galasa.examples 
 * <code>docker.registry.LOCAL.prefix=dockerhub/</code>
 * 
 * @galasa.extra
 * If the Docker Registry requires credentials for authentication, then the id for the credentials must be provided using the CPS property 
 * <code>docker.registry.ID.credentials</code> or <code>docker.registry.credentials</code>
 * 
 */
public class DockerRegistryPrefix extends CpsProperties {

    public static String get(DockerRegistryImpl dockerRegistry) throws DockerManagerException {
        String id = dockerRegistry.getId();
        String dockerRegistryPrefix = "";
        try {
            dockerRegistryPrefix = getStringNulled(DockerPropertiesSingleton.cps(), "registry", "prefix", id, "image");
            // Default value
            if (dockerRegistryPrefix == null) {
            	return "";
            }
            return dockerRegistryPrefix + "/";
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException("Problem asking the CPS for the docker registry type", e);
        }
    }
}
