/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.ZosBatchManagerException;

/**
 * zOS Batch default input class
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.default.[imageid].input.class
 * 
 * @galasa.description The default input class to set on the job card for submitted jobs
 * 
 * @galasa.required No
 * 
 * @galasa.default A
 * 
 * @galasa.valid_values a valid JES input class literal
 * 
 * @galasa.examples 
 * <code>zosbatch.default.MVSA.input.class=S</code><br>
 * <code>zosbatch.default.input.class=A</code>
 * 
 */
public class MsgClass extends CpsProperties {
    
    private static final String DEFAULT_CLASS = "A";
    
    public static String get(IZosImage image) throws ZosBatchManagerException {
        try {
            String msgClass = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "default", "message.class", image.getImageID());

            if (msgClass == null) {
                return DEFAULT_CLASS;
            } 
            
            return msgClass;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the zOSMF default message class for zOS image "  + image.getImageID(), e);
        }
    }

}
