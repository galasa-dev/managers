package dev.galasa.docker.internal.properties;

import java.util.ArrayList;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Manager Registry location
 * <p>
 * The URI's to any docker registry that is required to pull images from. Comma seperated list.
 * </p><p>
 * The property is:<br>
 * {@code docker.default.registries=DOCKERHUB,ARTIFACTORY1,...} 
 * </p>
 * <p>
 * The docker hub registry is always added to the list of acceptable registries.
 * </p>
 *
 */
public class DockerRegistry extends CpsProperties {

    public static String[] get() throws DockerManagerException {
        ArrayList<String> ids = new ArrayList<String>();
        try {
            String registryIds = getStringNulled(DockerPropertiesSingleton.cps(), "default", "registries");
            if (registryIds != null) {
				String[] list = registryIds.split(",");

				for (String i: list) {
					ids.add(i);
				}
			}
			String central = "DOCKERHUB";
			
			if (ids.isEmpty()) {
				ids.add(central);
			}

            return ids.toArray(new String[ids.size()]);
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException("Problem asking the CPS for the max slots for the docker server: " , e);
        }
    }
}