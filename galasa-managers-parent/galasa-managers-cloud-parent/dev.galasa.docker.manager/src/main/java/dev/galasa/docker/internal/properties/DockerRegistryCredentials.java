package dev.galasa.docker.internal.properties;

import java.net.URL;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerRegistryImpl;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Registry Credentials CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.registry.[ID.]credentials
 * 
 * @galasa.description Provides the credentials of a Docker Registry that is used by the Docker Manager
 * 
 * @galasa.required Yes if the registry requires authentication. 
 * 
 * @galasa.default DOCKER
 * 
 * @galasa.valid_values A valid credentials ID.
 * 
 * @galasa.examples 
 * <code>docker.registry.LOCAL.credentials=CREDSID</code>
 * 
 * @galasa.extra
 * If the <code>docker.registry.ID.credentials</code> CPS property is missing, the Docker Manager will attempt to use
 * the credentials ID that is provided in <code>docker.registry.credentials</code>, if that is missing, then the default credentials 
 * ID of <code>DOCKER</code> will be used.
 * 
 */
public class DockerRegistryCredentials extends CpsProperties {

    public static URL get(DockerRegistryImpl dockerRegistry) throws DockerManagerException {
        throw new UnsupportedOperationException("Not developed yet");
    }
}
