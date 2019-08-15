package dev.galasa.common.linux.spi;

import dev.galasa.common.linux.ILinuxManager;

public interface ILinuxManagerSpi extends ILinuxManager {
	
	void registerProvisioner(ILinuxProvisioner provisioner);

}
