package dev.galasa.linux.spi;

import dev.galasa.linux.ILinuxManager;

public interface ILinuxManagerSpi extends ILinuxManager {
	
	void registerProvisioner(ILinuxProvisioner provisioner);

}
