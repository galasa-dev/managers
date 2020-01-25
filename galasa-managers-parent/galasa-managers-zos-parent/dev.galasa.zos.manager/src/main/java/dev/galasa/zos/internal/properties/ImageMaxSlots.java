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
 * Maximum slots for zOS Image
 * <p>
 * The maximum slots available on a zOS Image for the specified tag
 * </p><p>
 * The property is:<br>
 * {@code zos.image.[tag].max.slots=2} 
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_MAX_SLOTS}
 * </p>
 *
 */
public class ImageMaxSlots extends CpsProperties {
    
    private static final int DEFAULT_MAX_SLOTS = 2;
    
    public static int get(String imageId) throws ZosManagerException {
        try {
            String slots = getStringNulled(ZosPropertiesSingleton.cps(), "image", "max.slots", imageId);
            if (slots == null)  {
                return DEFAULT_MAX_SLOTS;
            }
            return Integer.parseInt(slots);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the zOS image "  + imageId+ " max slots", e);
        }
    }

}
