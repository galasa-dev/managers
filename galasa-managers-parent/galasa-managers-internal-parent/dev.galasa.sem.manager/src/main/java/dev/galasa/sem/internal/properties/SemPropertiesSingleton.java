/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.sem.SemManagerException;

@Component(service=SemPropertiesSingleton.class, immediate=true)
public class SemPropertiesSingleton {
    
    private static SemPropertiesSingleton singletonInstance;

    private static void setInstance(SemPropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws SemManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new SemManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws SemManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new SemManagerException("Attempt to set manager CPS before instance created");
    }
}
