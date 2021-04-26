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

import java.util.Arrays;
import java.util.List;

/**
 * OpenStack windows Security Groups
 * <p>
 * Provide the names of the Security Groups that OpenStack will apply to the image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.windows.[imagename].securitygroup=default,galasa<br>
 * openstack.windows.default.securitygroup=galasa<br>
 * Where securitygroup is that provided in {@link WindowsImages}<br>
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author James Davies
 *
 */
public class WindowsSecurityGroups extends CpsProperties {

    public static @NotNull List<String> get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        String groups = getStringNulled(OpenstackPropertiesSingleton.cps(), "windows", "securitygroups", image);
        if (groups == null) {
            return null;
        }
        return Arrays.asList(groups.split(","));
    }

}
