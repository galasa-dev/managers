/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * zOS Program LanguageExtended data set prefix
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.[language].[imageid].dataset.prefix
 * 
 * @galasa.description zOS Program LanguageExtended data set High Level Qualifier
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values
 * 
 * @galasa.examples 
 * <code>zosprogram.cobol.MVSA.dataset.prefix=IGY.V6R3M0</code><br>
 * <code>zosprogram.cobol.dataset.prefix=SYS1.COBIL,IGY.V6R3M0</code>
 *
 */
public class ProgramLanguageDatasetPrefix extends CpsProperties {

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            List<String> datasetPrefixValue = getStringList(ZosProgramPropertiesSingleton.cps(), language.toString().toLowerCase(), "dataset.prefix", imageId);

            if (datasetPrefixValue.isEmpty()) {
                throw new ZosProgramManagerException("Required property zosprogram." + language.toString().toLowerCase() + ".[imageid].dataset.prefix not supplied");
            }
            return datasetPrefixValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program " + language + " dataset prefix for zOS image "  + imageId, e);
        }
    }

}
