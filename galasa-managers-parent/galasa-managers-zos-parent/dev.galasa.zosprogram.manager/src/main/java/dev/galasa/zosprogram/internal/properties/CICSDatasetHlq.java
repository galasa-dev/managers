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
import dev.galasa.zosprogram.ZosProgramManagerException;
//TODO: Should we get this from the CICS Manager?
/**
 * zOS CICS data set HLQ
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.cics.[imageid].dataset.hlq
 * 
 * @galasa.description zOS CICS data set High Level Qualifier
 * 
 * @galasa.required No
 * 
 * @galasa.default COBOL: CICS
 * 
 * @galasa.valid_values
 * 
 * @galasa.examples 
 * <code>zosprogram.cics.MVSA.dataset.hlq=CICS</code><br>
 * <code>zosprogram.cics.default.dataset.hlq=SYS1,CICS</code>
 *
 */
public class CICSDatasetHlq extends CpsProperties {

    private static final String DEFAULT_CICS_HLQ = "CICS";

    public static List<String> get(String imageId) throws ZosProgramManagerException {
        try {
            List<String> datasetHlqValue = getStringList(ZosProgramPropertiesSingleton.cps(), "cics", "dataset.hlq", imageId);

            if (datasetHlqValue.isEmpty()) {
                datasetHlqValue = Arrays.asList(DEFAULT_CICS_HLQ);
            }
            return datasetHlqValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program CICS dataset HLQ for zOS image "  + imageId, e);
        }
    }

}
