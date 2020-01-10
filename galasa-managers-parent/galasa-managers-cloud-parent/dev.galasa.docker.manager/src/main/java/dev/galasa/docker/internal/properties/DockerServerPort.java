package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerServerImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Manager Server Port
 * <p>
 * Provides which port is used to access the docker engine API
 * </p><p>
 * The property is:<br>
 * {@code docker.engine.server.port=2376} 
 * </p>
 * <p>
 * No Default, and is required to be set to use docker manager.
 * </p>
 *
 */
public class DockerServerPort extends CpsProperties {

    public static String get(DockerServerImpl dockerServerImpl) throws DockerManagerException {
		try {
			String dockerServerUri = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "server.port", dockerServerImpl.getServerId());

			if (dockerServerUri == null) {
				throw new DockerManagerException("Could not find a docker server port in CPS.");
			}
			return dockerServerUri;
		} catch (ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the docker server port", e);
	    }
    }
}