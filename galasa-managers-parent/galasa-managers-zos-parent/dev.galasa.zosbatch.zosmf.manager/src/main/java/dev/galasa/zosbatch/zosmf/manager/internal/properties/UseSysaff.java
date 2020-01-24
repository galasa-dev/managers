/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal.properties;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOS Batch job use SYSAFF
 * <p>
 * Use the run the zOS Batch job on the specified image by specifying<br>
 * {@code /*JOBPARM SYSAFF=[imageid]}
 * </p><p>
 * The property is:<br>
 * {@code zosbatch.batchjob.[imageid].use.sysaff=true}
 * </p>
 * <p>
 * The default value is true
 * </p>
 *
 */
public class UseSysaff extends CpsProperties {

    public static boolean get(String imageId) throws ZosBatchManagerException {
        try {
            String sysaffString = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "batchjob", "use.sysaff", imageId);
            return Boolean.parseBoolean(sysaffString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the batch job use SYSAFF property for zOS image "  + imageId, e);
        }
    }

}
