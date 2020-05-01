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
 * Extra bundle to required to implement the zOS UNIX Command Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.unix.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS UNIX Command Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.zosunix.ssh.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.unix.manager=dev.galasa.zosunix.ssh.manager</code>
 *
 */
public class UNIXExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_BUNDLE_EXTRA_FILE_MANAGER = "dev.galasa.zosunix.ssh.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String fileBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "unix.manager");
            if (fileBundleName == null)  {
                return DEFAULT_BUNDLE_EXTRA_FILE_MANAGER;
            }
            return fileBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the zOS UNIX Command Manager extra bundle name", e); 
        }
    }
}
