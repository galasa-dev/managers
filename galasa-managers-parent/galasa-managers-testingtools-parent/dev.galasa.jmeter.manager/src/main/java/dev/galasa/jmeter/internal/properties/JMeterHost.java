/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.jmeter.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.jmeter.JMeterManagerException;

public class JMeterHost extends CpsProperties {
    public static String get() throws JMeterManagerException {
        try {
           String jMeterTargetHost = getStringNulled(JMeterPropertiesSingleton.cps(), "target", "host");

           if (jMeterTargetHost == null) {
               throw new JMeterManagerException("Could not retrieve the targeted host address from the CPS");
           }
           return jMeterTargetHost;
        } catch (ConfigurationPropertyStoreException e) {
            throw new JMeterManagerException("Problem asking for the CPS-property for the Hostname",e);
        }
    }
}