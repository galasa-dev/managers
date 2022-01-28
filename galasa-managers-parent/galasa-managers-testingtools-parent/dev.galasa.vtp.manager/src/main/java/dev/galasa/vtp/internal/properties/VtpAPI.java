/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.vtp.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.vtp.manager.VtpManagerException;

/**
 * Extra bundle required to implement the zOS Batch Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.batch.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS Batch Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.common.zosbatch.zosmf.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.batch.manager=dev.galasa.common.zosbatch.zosmf.manager</code><br>
 *
 */
public class VtpAPI extends CpsProperties {
    
    public static boolean get() throws VtpManagerException {
        try {
            String vtpAPI = getStringNulled(VtpPropertiesSingleton.cps(), "API", "enable");
            if (vtpAPI == null)  {
                return false;
            }
            return Boolean.parseBoolean(vtpAPI);
        } catch (ConfigurationPropertyStoreException e) {
            throw new VtpManagerException("Problem asking CPS for the VTP recording enable status", e); 
        }
    }
}
