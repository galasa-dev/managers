package io.ejat.ipnetwork.spi;

import io.ejat.ipnetwork.IIpHost;
import io.ejat.ipnetwork.IIpPort;
import io.ejat.ipnetwork.IpNetworkManagerException;

public interface IIpHostSpi extends IIpHost {

	IIpPort provisionPort(String type) throws IpNetworkManagerException;

}
