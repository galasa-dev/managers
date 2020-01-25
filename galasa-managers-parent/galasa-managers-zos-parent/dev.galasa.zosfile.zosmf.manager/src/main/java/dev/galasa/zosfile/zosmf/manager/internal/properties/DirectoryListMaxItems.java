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
 * The maximum number of items from a directory list
 * <p>
 * The maximum number of items zOSMF returns when listing the content of a directory
 * </p><p>
 * The property is:<br>
 * {@code zosfile.unix.[imageid].directory.list.max.items=1000}
 * </p>
 * <p>
 * The default value is {@value #MAX_ITEMS}
 * </p>
 *
 */
public class DirectoryListMaxItems extends CpsProperties {

    private static final int MAX_ITEMS = 1000;

    public static int get(String imageId) throws ZosFileManagerException {
        try {
            String maxItemsString = getStringNulled(ZosFileZosmfPropertiesSingleton.cps(), "unix", "directory.list.max.items", imageId);

            if (maxItemsString == null) {
                return MAX_ITEMS;
            } else {
                return Integer.parseInt(maxItemsString);
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosFileManagerException("Problem asking the CPS for the directory list max items property for zOS image "  + imageId, e);
        }
    }

}
