/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.windows.spi;

import dev.galasa.windows.IWindowsManager;

public interface IWindowsManagerSpi extends IWindowsManager {

    void registerProvisioner(IWindowsProvisioner provisioner);

}
