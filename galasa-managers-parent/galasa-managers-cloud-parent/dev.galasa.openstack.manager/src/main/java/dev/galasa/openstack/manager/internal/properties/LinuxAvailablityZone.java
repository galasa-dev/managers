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
 * OpenStack Linux Availability Zone
 * <p>
 * Provide the availability zone to use when building the instance
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.[imagename].availability.zone=nova<br>
 * openstack.linux.default.availability.zone=nova<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * </p>
 * <p>
 * The default is nova
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class LinuxAvailablityZone extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "nova", "linux", "availability.zone", image);

    }

}
