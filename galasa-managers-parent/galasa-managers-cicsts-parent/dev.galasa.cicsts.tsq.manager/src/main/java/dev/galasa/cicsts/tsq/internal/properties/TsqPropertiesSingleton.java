/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.tsq.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cicsts.TsqManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=TsqPropertiesSingleton.class, immediate=true)
public class TsqPropertiesSingleton {
    
    private static TsqPropertiesSingleton singletonInstance;
    private static void setInstance(TsqPropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws TsqManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new TsqManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws TsqManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new TsqManagerException("Attempt to set manager CPS before instance created");
    }
}
