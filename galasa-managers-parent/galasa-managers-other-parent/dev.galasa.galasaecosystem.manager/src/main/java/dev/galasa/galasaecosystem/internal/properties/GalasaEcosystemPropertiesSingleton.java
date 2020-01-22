package dev.galasa.galasaecosystem.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.kubernetes.KubernetesManagerException;

@Component(service = GalasaEcosystemPropertiesSingleton.class, immediate = true)
public class GalasaEcosystemPropertiesSingleton {

    private static GalasaEcosystemPropertiesSingleton singletonInstance;
    private static void setInstance(GalasaEcosystemPropertiesSingleton instance){
        singletonInstance = instance;
	}
	
	private IConfigurationPropertyStoreService cps;

    @Activate
	public void activate() {
		setInstance(this);
	}
	
	@Deactivate
	public void deacivate() {
		setInstance(null);
    }
    
    public static IConfigurationPropertyStoreService cps() throws KubernetesManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new KubernetesManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws KubernetesManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new KubernetesManagerException("Attempt to set manager CPS before instance created");
	}
}