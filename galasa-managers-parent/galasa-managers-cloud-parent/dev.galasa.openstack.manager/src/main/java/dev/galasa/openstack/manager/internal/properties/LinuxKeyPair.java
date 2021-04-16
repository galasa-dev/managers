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
 * OpenStack Linux Key Pair
 * <p>
 * Provide the registered Key Pair that OpenStack will use when deploying the image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.[imagename].keypair=galasa<br>
 * openstack.linux.default.keypair=galasa<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * </p>
 * <p>
 * The default is galasa
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class LinuxKeyPair extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "cloudusr", "linux", "keypair", image);

    }

}
