/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.jmeter.internal.JMeterManagerException;

@Component(service = JMeterPropertiesSingleton.class, immediate =  true )
public class JMeterPropertiesSingleton {
    
    private static JMeterPropertiesSingleton singleton;

    private IConfigurationPropertyStoreService cps;


    private static void setSingleton(JMeterPropertiesSingleton instance) {
        singleton = instance;
    } 

    @Activate
    public void activate() {
        setSingleton(this);
    }

    @Deactivate
    public void deactivate() {
        setSingleton(null);
    }

    public static IConfigurationPropertyStoreService cps() throws JMeterManagerException {
        if (singleton != null) {
            return singleton.cps();
        }

        throw new JMeterManagerException("Attempting to acces the CPS before it has been initialised.");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws JMeterManagerException {
        if (singleton != null) {
            singleton.cps = cps;
            return;
        }

        throw new JMeterManagerException("Attempting to set the set before the instance is available.");
    }
}