/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile.zosmf.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosfile.ZosFileManagerException;

/**
 * Restrict processing to the zOSMF server on the specified image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosfile.zosmf.[imageid].restrict.to.image
 * 
 * @galasa.description Use only the zOSMF server running on the image associated with the zOS data set or file
 * 
 * @galasa.required No
 * 
 * @galasa.default False
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosfile.zosmf.restrict.to.image=true</code><br>
 * <cods>zosfile.zosmf.SYSA.restrict.to.image=true</code>
 *
 */
public class RestrictZosmfToImage extends CpsProperties {

    private static final boolean RESTRICT_TO_IMAGE = false;

    public static boolean get(String imageId) throws ZosFileManagerException {
        try {
            String restrictString = getStringNulled(ZosFileZosmfPropertiesSingleton.cps(), "zosmf", "restrict.zosmf.to.image", imageId);

            if (restrictString == null) {
                return RESTRICT_TO_IMAGE;
            } else {
                return Boolean.parseBoolean(restrictString);
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosFileManagerException("Problem asking the CPS for the restrict zOSMF to image property for zOS image "  + imageId, e);
        }
    }

}
