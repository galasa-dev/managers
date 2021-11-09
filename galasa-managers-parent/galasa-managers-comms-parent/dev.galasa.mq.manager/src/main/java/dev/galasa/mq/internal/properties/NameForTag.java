/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.mq.MqManagerException;

/**
 * The Name of the Queue Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[tag].name
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
 * <code>mq.server.[tag].name=QM1</code><br>
 *
 */
public class NameForTag extends CpsProperties {
    
    public static String get(@NotNull String tag) throws MqManagerException {
        try {
            return getStringNulled(MqPropertiesSingleton.cps(), "server", "name", tag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new MqManagerException("Problem asking the CPS for the queue manager name for tag '"  + tag + "'", e);
        }
    }

}
