/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DefaultLogonInitialText extends CpsProperties {

    public static String get() throws CicstsManagerException {
        try {
            return getStringNulled(CicstsPropertiesSingleton.cps(), "default.logon", "initial.text");
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the default logon initial text", e); 
        }
    }
}
