package dev.galasa.docker.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;
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
 * {@code docker.default.registries=uri1,uri2,...} 
 * </p>
 * <p>
 * The docker hub registry is always added to the list of acceptable registries.
 * </p>
 *
 */
public class DockerRegistry extends CpsProperties {

    public static URL[] get() throws DockerManagerException {
        ArrayList<URL> urls = new ArrayList<URL>();
		try {
			String registries = getStringNulled(DockerPropertiesSingleton.cps(), "default", "registries");
            if (registries != null) {
				String[] regs = registries.split(",");
				for(String reg : regs) {
					URL url = new URL(reg);
					if (!urls.contains(url)) {
						urls.add(url);
					}
				}
			}
            // UNSURE IF TO INCLUDE THIS AS NOT ALL CUSTOMERS WILL WANT/CAN USE THIS
			URL central = new URL("https://registry.hub.docker.com");
			if (urls.isEmpty()) {
				urls.add(central);
			}

			return urls.toArray(new URL[urls.size()]);
		} catch (ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the max slots for the docker server: " , e);
		} catch (MalformedURLException e) {
            throw new DockerManagerException("Could not parse the returned registries from CPS into a URL", e);
        }
	}
}