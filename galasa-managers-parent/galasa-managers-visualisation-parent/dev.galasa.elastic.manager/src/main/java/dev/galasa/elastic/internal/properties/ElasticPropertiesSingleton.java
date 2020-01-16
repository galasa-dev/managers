
package dev.galasa.elastic.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.elastic.ElasticManagerException;

@Component(service = ElasticPropertiesSingleton.class, immediate = true)
public class ElasticPropertiesSingleton {

    private static ElasticPropertiesSingleton  INSTANCE;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        INSTANCE = this;
    }

    @Deactivate
    public void deacivate() {
        INSTANCE = null;
    }

    public static IConfigurationPropertyStoreService cps() throws ElasticManagerException {
        if (INSTANCE != null) {
            return INSTANCE.cps;
        }

        throw new ElasticManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws ElasticManagerException {
        if (INSTANCE != null) {
            INSTANCE.cps = cps;
            return;
        }

        throw new ElasticManagerException("Attempt to set manager CPS before instance created");
    }
}
