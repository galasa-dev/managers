package dev.galasa.docker.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerRegistryImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DockerRegistryURL extends CpsProperties {

    public static URL get(DockerRegistryImpl dockerRegistry) throws DockerManagerException {
        String id = dockerRegistry.getId();
        String dockerRegistryURL = "";
        try {
            dockerRegistryURL = getStringNulled(DockerPropertiesSingleton.cps(), "registry", "URL", id);
            // Default value
            if (dockerRegistryURL == null) {
                if("DOCKERHUB".equals(id)) {
                    return new URL("https://registry.hub.docker.com");
                }
                throw new DockerManagerException("Could not find a docker registry type in CPS for : " + id);
            }
        return new URL(dockerRegistryURL);
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException("Problem asking the CPS for the docker registry type", e);
        } catch (MalformedURLException e) {
            throw new DockerManagerException("Could not parse the url returned from CPS: " + dockerRegistryURL, e);
        }
    }
}
