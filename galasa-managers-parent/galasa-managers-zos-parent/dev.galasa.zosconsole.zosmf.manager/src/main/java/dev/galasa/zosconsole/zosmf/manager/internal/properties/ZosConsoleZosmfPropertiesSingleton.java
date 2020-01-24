/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole.zosmf.manager.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=ZosConsoleZosmfPropertiesSingleton.class, immediate=true)
public class ZosConsoleZosmfPropertiesSingleton {
    
    private static ZosConsoleZosmfPropertiesSingleton singletonInstance;
    private static void setInstance(ZosConsoleZosmfPropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws ZosConsoleManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new ZosConsoleManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws ZosConsoleManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new ZosConsoleManagerException("Attempt to set manager CPS before instance created");
    }
}
