package dev.voras.common.ipnetwork.spi;

import javax.validation.constraints.NotNull;

import dev.voras.common.ipnetwork.IpNetworkManagerException;

public interface IIpNetworkManagerSpi {

	@NotNull
	IIpHostSpi buildHost(String hostId) throws IpNetworkManagerException;

}
