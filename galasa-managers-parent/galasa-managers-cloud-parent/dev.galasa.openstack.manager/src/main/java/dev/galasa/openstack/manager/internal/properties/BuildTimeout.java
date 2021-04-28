/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2021.
 */
package dev.galasa.openstack.manager.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Build Timeout value
 * <p>
 * In minutes, how long the OpenStack Manager should wait for Compute to build
 * and start the server.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.build.timeout=9
 * </p>
 * <p>
 * default value is 10 minutes
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class BuildTimeout extends CpsProperties {

    public static int get() throws OpenstackManagerException {
        return getIntWithDefault(OpenstackPropertiesSingleton.cps(), 10, "build", "timeout");
    }

}
