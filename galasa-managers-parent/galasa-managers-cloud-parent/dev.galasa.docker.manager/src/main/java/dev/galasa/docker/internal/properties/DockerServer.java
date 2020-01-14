package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;


/**
 * Docker Engine CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.engine.server
 * 
 * @galasa.description Provides location of the Docker Engine
 * 
 * @galasa.required Yes - the hostname of the Docker Engine must be provided
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid DNS name or IPv4/6 address
 * 
 * @galasa.examples 
 * <code>docker.engine.server=docker.example.company.org<br>
 * docker.engine.server=192.168.2.3
 * </code>
 * 
 * @galasa.extra
 * Currently, the Docker Manager supports only a single Docker Engine although it is planned to allow multiple Engines to be configured.<br>
 * To allow local runs to access the local Docker Engine, you must add this property to the CPS and enable the TCP port of your local Docker Engine.<br>
 * If the Docker Engine is not using the default TCP port, you must provide the *docker.engine.port* configuration property in the CPS.
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