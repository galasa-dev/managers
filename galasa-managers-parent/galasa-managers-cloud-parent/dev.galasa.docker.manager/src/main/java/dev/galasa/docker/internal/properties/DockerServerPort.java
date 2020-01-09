package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Engine Server Port CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.engine.server.port
 * 
 * @galasa.description Provides TCP Port of the Docker server
 * 
 * @galasa.required No
 * 
 * @galasa.default 2375
 * 
 * @galasa.valid_values Any valid TCP Port number
 * 
 * @galasa.examples 
 * <code>docker.engine.server.port=2375</code>
 * 
 * @galasa.extra
 * The Docker Manager will communicate with the Docker Engine Server via TCP.   The Docker Engine Server will need to be 
 * configured to open the TCP port, which will normally be 2375.  If the port is not the default one, then this property will need to be provided in the CPS.
 * 
 */

public class DockerServerPort extends CpsProperties {

    public static String get() throws DockerManagerException {
		try {
			String dockerServerUri = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "server.port");

			if (dockerServerUri == null) {
				throw new DockerManagerException("Could not find a docker server port in CPS.");
			}
			return dockerServerUri;
		} catch (ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the docker server port", e);
	    }
    }
}