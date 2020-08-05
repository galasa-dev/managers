/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * zOS LanguageExtended Environment data set prefix
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.le.[imageid].dataset.prefix
 * 
 * @galasa.description zOS LanguageExtended Environment data set prefix
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values
 * 
 * @galasa.examples 
 * <code>zosprogram.le.MVSA.dataset.prefix=CEE</code><br>
 * <code>zosprogram.le.dataset.prefix=SYS1.LE,CEE</code>
 *
 */
public class LanguageEnvironmentDatasetPrefix extends CpsProperties {

    public static List<String> get(String imageId) throws ZosProgramManagerException {
        try {
            List<String> datasetPrefixValue = getStringList(ZosProgramPropertiesSingleton.cps(), "le", "dataset.prefix", imageId);
            if (datasetPrefixValue.isEmpty()) {
                throw new ZosProgramManagerException("Required property zosprogram.le.[imageid].dataset.prefix not supplied");
            }
            return datasetPrefixValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS LanguageExtended Environment dataset prefix for zOS image "  + imageId, e);
        }
    }

}
