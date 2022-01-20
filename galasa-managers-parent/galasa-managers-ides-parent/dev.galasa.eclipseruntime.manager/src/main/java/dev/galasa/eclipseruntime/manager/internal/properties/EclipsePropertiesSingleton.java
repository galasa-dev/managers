/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.eclipseruntime.manager.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.eclipseruntime.EclipseManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service = EclipsePropertiesSingleton.class, immediate = true)
public class EclipsePropertiesSingleton {

    private static EclipsePropertiesSingleton singletonInstance;
    private static void setInstance(EclipsePropertiesSingleton instance){
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
    
    public static IConfigurationPropertyStoreService cps() throws EclipseManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new EclipseManagerException("Attempted to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws EclipseManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new EclipseManagerException("Attempt to set manager CPS before instance created");
	}
}