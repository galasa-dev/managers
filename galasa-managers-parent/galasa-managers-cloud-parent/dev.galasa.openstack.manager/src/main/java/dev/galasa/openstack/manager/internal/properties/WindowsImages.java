/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.openstack.manager.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Windows images
 * <p>
 * A comma separated list of what images are available to build servers from.
 * </p>
 * <p>
 * The cascading properties can be:-<br>
 * <br>
 * openstack.windows.[version].images=win10,win10spi8<br>
 * openstack.windows.images=win10<br>
 * Where version is
 * version string<br>
 * Example openstack.windows.10.images=win10
 * </p>
 * <p>
 * There are no defaults
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class WindowsImages extends CpsProperties {

    public static @NotNull List<String> get(String version)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        if (version != null) {
            return getStringList(OpenstackPropertiesSingleton.cps(), "windows", "images", version);
        }

        return getStringList(OpenstackPropertiesSingleton.cps(), "windows", "images");

    }

}
