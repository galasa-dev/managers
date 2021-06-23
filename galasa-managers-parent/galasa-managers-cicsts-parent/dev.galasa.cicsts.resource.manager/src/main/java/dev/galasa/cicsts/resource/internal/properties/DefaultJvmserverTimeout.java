/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.resource.internal.properties;

import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DefaultJvmserverTimeout extends CpsProperties {

    public static int get() throws CicsResourceManagerException {
            return getIntWithDefault(CicstsResourcePropertiesSingleton.cps(), 10000, "jvmserver", "default.timeout");
    }
}
