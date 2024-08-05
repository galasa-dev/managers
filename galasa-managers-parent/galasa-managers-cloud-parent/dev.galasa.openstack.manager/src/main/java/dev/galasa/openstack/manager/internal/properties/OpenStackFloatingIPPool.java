/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Floating IP Pool
 * <p>
 * The Openstack Floating IP Pool that the OpenStack Manager will use
 * to create a Floating IP address within.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.server.floatingip.pool=my_network_name
 * </p>
 * <p>
 * There is no default
 * </p>
 *
 */
public class OpenStackFloatingIPPool extends CpsProperties {

    public static String get()
            throws ConfigurationPropertyStoreException, OpenstackManagerException {
        return getStringNulled(OpenstackPropertiesSingleton.cps(), "server", "floatingip.pool");
    }

}
