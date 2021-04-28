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
 * OpenStack Linux Flavor
 * <p>
 * Provide the image flavor to use when building the instance
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.[imagename].flavor=m1.medium<br>
 * openstack.linux.default.flavor=m1.medium<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * </p>
 * <p>
 * The default is m1.medium
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class LinuxFlavor extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "m1.medium", "linux", "flavor", image);

    }

}
