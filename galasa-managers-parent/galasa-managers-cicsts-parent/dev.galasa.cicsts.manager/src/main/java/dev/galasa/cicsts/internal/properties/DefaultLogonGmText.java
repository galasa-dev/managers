/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DefaultLogonGmText extends CpsProperties {

    public static String get() throws CicstsManagerException {
            return getStringWithDefault(CicstsPropertiesSingleton.cps(), "DFHZC2312", "default.logon", "gm.text");
    }
}
