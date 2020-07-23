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
 * zOS Program Language data set HLQ
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.[language].[imageid].dataset.hlq
 * 
 * @galasa.description zOS Program Language data set High Level Qualifier
 * 
 * @galasa.required No
 * 
 * @galasa.default COBOL: IGY.V6R3M0
 * 
 * @galasa.valid_values
 * 
 * @galasa.examples 
 * <code>zosprogram.cobol.MVSA.dataset.hlq=IGY.V6R3M0</code><br>
 * <code>zosprogram.cobol.dataset.hlq=SYS1.COBIL,IGY.V6R3M0</code>
 *
 */
public class ProgramLanguageDatasetHlq extends CpsProperties {

    private static final String DEFAULT_COBOL_HLQ = "IGY.V6R3M0";

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            List<String> datasetHlqValue = getStringList(ZosProgramPropertiesSingleton.cps(), language.toString().toLowerCase(), "dataset.hlq", imageId);

            if (datasetHlqValue.isEmpty()) {
                switch (language) {
                case COBOL: 
                    datasetHlqValue = Arrays.asList(DEFAULT_COBOL_HLQ);
                    break;
                default:
                    throw new ZosProgramManagerException("Invalid program language: " + language);
                }
            }
            return datasetHlqValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program " + language + " dataset HLQ for zOS image "  + imageId, e);
        }
    }

}
