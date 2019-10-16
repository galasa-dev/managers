package dev.galasa.zosfile.zosmf.manager.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosfile.ZosFileManagerException;

@Component(service=ZosFileZosmfPropertiesSingleton.class, immediate=true)
public class ZosFileZosmfPropertiesSingleton {
	
	private static ZosFileZosmfPropertiesSingleton singletonInstance;
	private static void setInstance(ZosFileZosmfPropertiesSingleton instance) {
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
	
	public static IConfigurationPropertyStoreService cps() throws ZosFileManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new ZosFileManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws ZosFileManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new ZosFileManagerException("Attempt to set manager CPS before instance created");
	}
}
