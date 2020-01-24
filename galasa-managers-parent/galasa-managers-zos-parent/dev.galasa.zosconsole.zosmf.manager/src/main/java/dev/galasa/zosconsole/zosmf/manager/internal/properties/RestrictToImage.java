/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole.zosmf.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosconsole.ZosConsoleManagerException;

/**
 * Restrict zOS console processing to the zOSMF server on the specified image
 * <p>
 * Use only the zOSMF server running on the image associated with the zOS Console
 * </p><p>
 * The property is:<br>
 * {@code zosconsole.console.[imageid].restrict.to.image=true}
 * </p>
 * <p>
 * The default value is false
 * </p>
 *
 */
public class RestrictToImage extends CpsProperties {

    public static boolean get(String imageId) throws ZosConsoleManagerException {
        try {
            String restritToImageString = getStringNulled(ZosConsoleZosmfPropertiesSingleton.cps(), "zosconsole", "restrict.to.image", imageId);
            return Boolean.parseBoolean(restritToImageString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosConsoleManagerException("Problem asking the CPS for the console restrict to image property for zOS image "  + imageId, e);
        }
    }

}
