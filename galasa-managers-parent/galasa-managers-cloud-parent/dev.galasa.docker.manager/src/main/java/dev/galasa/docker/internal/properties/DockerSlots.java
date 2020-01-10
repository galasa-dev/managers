package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerServerImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;


/**
 * Docker Manager Slots
 * <p>
 * Throttling property, used to limit the number of parallel running containers
 * </p><p>
 * The property is:<br>
 * {@code docker.engine.server.<dockerServer>.max.slots=numberOfSlots} 
 * </p>
 * <p>
 * No Default, and is required to be set to use docker manager.
 * </p>
 *
 */
public class DockerSlots extends CpsProperties {
    public static String get(DockerServerImpl dockerServer) throws DockerManagerException {
		try {
			String maxSlots = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "server.max.slots", dockerServer.getServerId());

			if (maxSlots == null) {
				throw new DockerManagerException("Value for Docker Server max slots not configured for the docker server: "  + dockerServer);
			}
			return maxSlots;
		} catch (ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the max slots for the docker server: "  + dockerServer, e);
		}
	}

}