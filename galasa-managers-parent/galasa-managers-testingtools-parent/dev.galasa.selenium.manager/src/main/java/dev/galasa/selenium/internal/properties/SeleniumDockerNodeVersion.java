/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Selenium Driver Version for Docker Node
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.docker.node.version
 * 
 * @galasa.description Provides the version number for the docker image that will be used for both the provisioing of docker and kubernetes selenium nodes.
 * 
 * @galasa.required no
 * 
 * @galasa.valid_values 4.0.0-beta-2-20210317
 * 
 * @galasa.examples 
 * <code>selenium.docker.node.version=4.0.0-beta-2-20210317</code>
 * 
 */
public class SeleniumDockerNodeVersion extends CpsProperties {

    public static String get() throws SeleniumManagerException {
        return getStringWithDefault(SeleniumPropertiesSingleton.cps(), "4.0.0-beta-2-20210317", "docker", "node.version");
    }

}