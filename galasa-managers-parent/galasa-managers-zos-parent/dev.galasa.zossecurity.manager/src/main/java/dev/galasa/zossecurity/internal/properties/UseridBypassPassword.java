/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Bypass Password
 */
public class UseridBypassPassword extends CpsProperties {
    
    public static boolean get() throws ZosSecurityManagerException {
        try {
        	return Boolean.parseBoolean(getStringNulled(ZosSecurityPropertiesSingleton.cps(), "userid", "allocate.bypass.password"));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Bypass Password");
        }
    }

}
