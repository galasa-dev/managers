/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.openstack.manager.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Windows Key Pair
 * <p>
 * Provide the registered Key Pair that OpenStack will use when deploying the image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.windows.[imagename].keypair=galasa<br>
 * openstack.windows.default.keypair=galasa<br>
 * Where imagename is that provided in {@link WindowsImages}<br>
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class WindowsKeyPair extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringNulled(OpenstackPropertiesSingleton.cps(), "windows", "keypair", image);

    }

}
