/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.linux.spi;

import dev.galasa.linux.ILinuxManager;

public interface ILinuxManagerSpi extends ILinuxManager {

    void registerProvisioner(ILinuxProvisioner provisioner);

}
