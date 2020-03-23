/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosbatch.ZosBatchManagerException;

/**
 * Restrict zOS batch processing to the zOSMF server on the specified image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.batchjob.[imageid].restrict.to.image
 * 
 * @galasa.description Use only the zOSMF server running on the image associated with the zOS Batch job
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>zosbatch.batchjob.MVSA.restrict.to.image=true</code><br>
 * <code>zosbatch.batchjob.default.restrict.to.image=false</code>
 *
 */
public class RestrictToImage extends CpsProperties {

    public static boolean get(String imageId) throws ZosBatchManagerException {
        try {
            String restritToImageString = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "batchjob", "restrict.to.image", imageId);
            return Boolean.parseBoolean(restritToImageString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the batch job restrict to image property for zOS image "  + imageId, e);
        }
    }

}
