/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.jmeter.JMeterManagerException;

public class JMeterPort extends CpsProperties {
    public static String get() throws JMeterManagerException {
        try {
            String portNumber = getStringNulled(JMeterPropertiesSingleton.cps(), "target", "port");

            if (portNumber == null){
                throw new JMeterManagerException("Could not find a port number in the CPS.");
            }
            return portNumber;
        } catch (ConfigurationPropertyStoreException e) {
            throw new JMeterManagerException("Could not assign the Port number of the target", e);
        }
    }
}