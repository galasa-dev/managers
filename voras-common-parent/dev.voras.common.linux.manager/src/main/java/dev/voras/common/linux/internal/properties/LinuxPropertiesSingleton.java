package dev.voras.common.linux.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.voras.common.linux.LinuxManagerException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;

@Component(service=LinuxPropertiesSingleton.class, immediate=true)
public class LinuxPropertiesSingleton {
	
	private static LinuxPropertiesSingleton INSTANCE;
	
	private IConfigurationPropertyStoreService cps;
	
	@Activate
	public void activate() {
		INSTANCE = this;
	}
	
	@Deactivate
	public void deacivate() {
		INSTANCE = null;
	}
	
	public static IConfigurationPropertyStoreService cps() throws LinuxManagerException {
		if (INSTANCE != null) {
			return INSTANCE.cps;
		}
		
		throw new LinuxManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws LinuxManagerException {
		if (INSTANCE != null) {
			INSTANCE.cps = cps;
			return;
		}
		
		throw new LinuxManagerException("Attempt to set manager CPS before instance created");
	}
}
