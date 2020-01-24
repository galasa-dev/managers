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
 * Extra bundle to required to implement the zOS Batch Manager
 * <p>
 * The name of the Bundle that implements the zOS Batch Manager 
 * </p><p>
 * The property is:<br>
 * {@code zos.bundle.extra.batch.manager=dev.galasa.common.zosbatch.zosmf.manager} 
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_BUNDLE_EXTRA_BATCH_MANAGER}
 * </p>
 *
 */
public class BatchExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_BUNDLE_EXTRA_BATCH_MANAGER = "dev.galasa.zosbatch.zosmf.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String batchBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "batch.manager");
            if (batchBundleName == null)  {
                return DEFAULT_BUNDLE_EXTRA_BATCH_MANAGER;
            }
            return batchBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the batch manager extra bundle name", e); 
        }
    }
}
