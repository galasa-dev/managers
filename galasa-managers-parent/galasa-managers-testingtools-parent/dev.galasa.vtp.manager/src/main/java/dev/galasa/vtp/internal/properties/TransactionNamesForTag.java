/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.vtp.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.vtp.manager.VtpManagerException;

/**
 * The Name of the Queue Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[instanceid].name
 * 
 * @galasa.description The queue manager name for the specified tag
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>mq.server.[instanceid].name=QM1</code><br>
 *
 */
public class TransactionNamesForTag extends CpsProperties {
    
    public static String get(@NotNull String instanceid) throws VtpManagerException {
        try {
            return getStringNulled(VtpPropertiesSingleton.cps(), "cics", "transactions", instanceid);
        } catch (ConfigurationPropertyStoreException e) {
            throw new VtpManagerException("Problem asking the CPS for the queue manager name for instance '"  + instanceid + "'", e);
        }
    }

}
