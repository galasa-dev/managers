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
 * Extra bundle to required to implement the zOS File Manager
 * <p>
 * The name of the Bundle that implements the zOS File Manager 
 * </p><p>
 * The property is:<br>
 * {@code zos.bundle.extra.file.manager=dev.galasa.common.zosfile.zosmf.manager} 
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_BUNDLE_EXTRA_FILE_MANAGER}
 * </p>
 *
 */
public class FileExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_BUNDLE_EXTRA_FILE_MANAGER = "dev.galasa.zosfile.zosmf.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String fileBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "file.manager");
            if (fileBundleName == null)  {
                return DEFAULT_BUNDLE_EXTRA_FILE_MANAGER;
            }
            return fileBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the file manager extra bundle name", e); 
        }
    }
}
