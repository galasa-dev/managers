/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2021.
 */
package dev.galasa.linux.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.ILinuxManager;
import dev.galasa.linux.LinuxManagerException;

public interface ILinuxManagerSpi extends ILinuxManager {

    void registerProvisioner(ILinuxProvisioner provisioner);

    ILinuxImage getImageForTag(@NotNull String imageTag) throws LinuxManagerException;

}
