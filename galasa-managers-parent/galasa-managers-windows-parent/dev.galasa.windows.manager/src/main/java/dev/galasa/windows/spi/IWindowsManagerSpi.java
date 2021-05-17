/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.windows.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.windows.IWindowsImage;
import dev.galasa.windows.IWindowsManager;
import dev.galasa.windows.WindowsManagerException;

public interface IWindowsManagerSpi extends IWindowsManager {

    void registerProvisioner(IWindowsProvisioner provisioner);

    IWindowsImage getImageForTag(@NotNull String imageTag) throws WindowsManagerException;

}
