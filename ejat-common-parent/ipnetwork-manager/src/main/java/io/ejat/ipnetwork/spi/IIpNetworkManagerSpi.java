package io.ejat.ipnetwork.spi;

import javax.validation.constraints.NotNull;

import io.ejat.ipnetwork.IpNetworkManagerException;

public interface IIpNetworkManagerSpi {

	@NotNull
	IIpHostSpi buildHost(String hostId) throws IpNetworkManagerException;

}
