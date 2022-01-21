/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq.internal.properties;

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
public class VtpEnable extends CpsProperties {
    
    public static boolean get() throws VtpManagerException {
        try {
            String vtpEnable = getStringNulled(VtpPropertiesSingleton.cps(), "recording", "enable");
            if (vtpEnable == null)  {
                return false;
            }
            return Boolean.parseBoolean(vtpEnable);
        } catch (ConfigurationPropertyStoreException e) {
            throw new VtpManagerException("Problem asking CPS for the VTP recording enable status", e); 
        }
    }
}
