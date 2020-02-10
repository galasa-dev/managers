/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal.properties;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOS Batch job truncate JCL
 * <p>
 * The z/OSMF submit job will fail if supplied with JCL records greater than 80 characters. Setting this property to true will truncate any records to 80
 * characters and issue a warning message.
 * </p><p>
 * The property is:<br>
 * {@code zosbatch.batchjob.[imageid].truncate.jcl.records=true}
 * </p>
 * <p>
 * The default value is true
 * </p>
 *
 */
public class TruncateJCLRecords extends CpsProperties {

    public static boolean get(String imageId) throws ZosBatchManagerException {
        try {
            String sysaffString = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "batchjob", "truncate.jcl.records", imageId);
            if (sysaffString == null || sysaffString.isEmpty()) {
                return true;
            }
            return Boolean.parseBoolean(sysaffString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the truncate JCL records property for zOS image "  + imageId, e);
        }
    }

}
