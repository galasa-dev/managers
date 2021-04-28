/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.linux.internal;

import java.util.List;

import dev.galasa.linux.OperatingSystem;
import dev.galasa.linux.spi.ILinuxProvisionedImage;
import dev.galasa.linux.spi.ILinuxProvisioner;

public class LinuxDefaultProvisioner implements ILinuxProvisioner {

    @Override
    public ILinuxProvisionedImage provisionLinux(String tag, OperatingSystem operatingSystem, List<String> capabilities) {

        // *** Boo

        return null;
    }

}
