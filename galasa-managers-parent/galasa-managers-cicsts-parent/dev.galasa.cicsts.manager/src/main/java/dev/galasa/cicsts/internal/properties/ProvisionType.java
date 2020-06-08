/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ProvisionType extends CpsProperties {

    public static String get() throws CicstsManagerException {
        return getStringWithDefault(CicstsPropertiesSingleton.cps(), "provisioned", "provision", "type").toUpperCase();
    }
}
