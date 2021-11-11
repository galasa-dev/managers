/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.mq.MqManagerException;

/**
 * The Host for the queueManager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[tag].host
 * 
 * @galasa.description The channel for the specified tag
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>mq.server.[tag].host=127.0.0.1</code><br>
 *
 */
public class QueueNameForTag extends CpsProperties {
    
    public static String get(@NotNull String tag) throws MqManagerException {
        try {
            return getStringNulled(MqPropertiesSingleton.cps(), "queue.tag", "queuename", tag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new MqManagerException("Problem asking the CPS for the instance id for tag '"  + tag + "'", e);
        }
    }

}
