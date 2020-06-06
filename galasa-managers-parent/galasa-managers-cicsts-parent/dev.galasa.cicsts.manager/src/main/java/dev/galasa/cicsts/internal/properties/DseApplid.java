/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DseApplid extends CpsProperties {

    public static String get(String tag) throws CicstsManagerException {
        try {
            return getStringNulled(CicstsPropertiesSingleton.cps(), "dse.tag." + tag, "applid").toUpperCase();
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the DSE applid for tag " + tag, e); 
        }
    }
}
