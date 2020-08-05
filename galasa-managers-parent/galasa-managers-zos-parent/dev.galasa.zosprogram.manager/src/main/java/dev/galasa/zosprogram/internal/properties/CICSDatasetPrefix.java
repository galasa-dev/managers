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
//TODO: Should we get this from the CICS Manager?
/**
 * zOS CICS data set Prefix
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.cics.[imageid].dataset.prefix
 * 
 * @galasa.description zOS CICS data set prefix
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values
 * 
 * @galasa.examples 
 * <code>zosprogram.cics.MVSA.dataset.prefix=CICS</code><br>
 * <code>zosprogram.cics.default.dataset.prefix=SYS1,CICS</code>
 *
 */
public class CICSDatasetPrefix extends CpsProperties {

    public static List<String> get(String imageId) throws ZosProgramManagerException {
        try {
            List<String> datasetPrefixValue = getStringList(ZosProgramPropertiesSingleton.cps(), "cics", "dataset.prefix", imageId);

            if (datasetPrefixValue.isEmpty()) {
                throw new ZosProgramManagerException("Required property zosprogram.cics.[imageid].dataset.prefix not supplied");
            }
            return datasetPrefixValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program CICS dataset prefix for zOS image "  + imageId, e);
        }
    }

}
