/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal.properties;

import java.util.Arrays;
import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * zOS Language Environment data set HLQ
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.le.[imageid].dataset.hlq
 * 
 * @galasa.description zOS Language Environment data set High Level Qualifier
 * 
 * @galasa.required No
 * 
 * @galasa.default CEE
 * 
 * @galasa.valid_values
 * 
 * @galasa.examples 
 * <code>zosprogram.le.MVSA.dataset.hlq=CEE</code><br>
 * <code>zosprogram.le.dataset.hlq=SYS1.LE,CEE</code>
 *
 */
public class LanguageEnvironmentDatasetHlq extends CpsProperties {

    private static final String DEFAULT_LE_HLQ = "CEE";

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            List<String> datasetHlqValue = getStringList(ZosProgramPropertiesSingleton.cps(), "le", "dataset.hlq", imageId);
            if (datasetHlqValue.isEmpty()) {
                return Arrays.asList(DEFAULT_LE_HLQ);
            }
            return datasetHlqValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS Language Environment dataset HLQ for zOS image "  + imageId, e);
        }
    }

}
