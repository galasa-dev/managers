package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;


/**
 * Docker Manager Server
 * <p>
 * Provides location of the Docker server
 * </p><p>
 * The property is:<br>
 * {@code docker.engine.server=127.0.0.1} 
 * </p>
 * <p>
 * No Default, and is required to be set to use docker manager.
 * </p>
 *
 */
public class DockerServer extends CpsProperties {

    public static String get() throws DockerManagerException {
		try {
			String dockerServer = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "server");

			if (dockerServer == null) {
				throw new DockerManagerException("Could not find a docker server in CPS.");
			}
			return dockerServer;
		} catch (ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the docker server URI", e);
        }
	}
}