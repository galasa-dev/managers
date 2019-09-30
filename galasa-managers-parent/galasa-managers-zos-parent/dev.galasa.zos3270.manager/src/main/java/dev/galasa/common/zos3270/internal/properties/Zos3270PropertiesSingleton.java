package dev.galasa.common.zos3270.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.common.zos3270.Zos3270ManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=Zos3270PropertiesSingleton.class, immediate=true)
public class Zos3270PropertiesSingleton {
	
	private static Zos3270PropertiesSingleton INSTANCE;
	
	private IConfigurationPropertyStoreService cps;
	
	@Activate
	public void activate() {
		INSTANCE = this;
	}
	
	@Deactivate
	public void deacivate() {
		INSTANCE = null;
	}
	
	public static IConfigurationPropertyStoreService cps() throws Zos3270ManagerException {
		if (INSTANCE != null) {
			return INSTANCE.cps;
		}
		
		throw new Zos3270ManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws Zos3270ManagerException {
		if (INSTANCE != null) {
			INSTANCE.cps = cps;
			return;
		}
		
		throw new Zos3270ManagerException("Attempt to set manager CPS before instance created");
	}
}
