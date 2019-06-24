package dev.voras.common.linux.spi;

import dev.voras.common.linux.ILinuxManager;

public interface ILinuxManagerSpi extends ILinuxManager {
	
	void registerProvisioner(ILinuxProvisioner provisioner);

}
