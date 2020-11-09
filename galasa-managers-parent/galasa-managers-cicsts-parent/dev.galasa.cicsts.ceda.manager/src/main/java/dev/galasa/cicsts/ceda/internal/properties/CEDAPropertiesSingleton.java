package dev.galasa.cicsts.ceda.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cicsts.ceda.CEDAManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=CEDAPropertiesSingleton.class, immediate=true)
public class CEDAPropertiesSingleton {
    private static CEDAPropertiesSingleton singletonInstance;
    private static void setInstance(CEDAPropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws CEDAManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new CEDAManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws CEDAManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new CEDAManagerException("Attempt to set manager CPS before instance created");
    }
    
}