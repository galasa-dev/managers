/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.mq.MqManagerException;

/**
 * The Name of the Queue Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name mq.server.[tag].port
 * 
 * @galasa.description The queue manager port for the specified tag
 * 
 * @galasa.required Yes
 * 
 * @galasa.default 1414
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>mq.server.[tag].port=1414</code><br>
 *
 */
public class PortForTag extends CpsProperties {
    
    public static int get(@NotNull String tag) throws MqManagerException {
    	return getIntWithDefault(MqPropertiesSingleton.cps(), 1414, "server", "port", tag);
    }

}
