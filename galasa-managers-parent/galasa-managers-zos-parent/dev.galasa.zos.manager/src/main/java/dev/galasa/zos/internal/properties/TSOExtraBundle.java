/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * Extra bundle to required to implement the zOS TSO Command Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.[imageid].tso.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS TSO Command Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.zostso.ssh.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.MVSA.tso.manager=dev.galasa.zostso.ssh.manager</code><br>
 * <code>zos.bundle.extra.tso.manager=dev.galasa.zostso.ssh.manager</code>
 *
 */
public class TSOExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_BUNDLE_EXTRA_TSO_MANAGER = "dev.galasa.zostso.ssh.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String fileBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "tso.manager");
            if (fileBundleName == null)  {
                return DEFAULT_BUNDLE_EXTRA_TSO_MANAGER;
            }
            return fileBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the zOS TSO Command Manager extra bundle name", e); 
        }
    }
}
