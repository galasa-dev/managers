package dev.voras.common.ipnetwork.spi;

import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.ipnetwork.IIpPort;
import dev.voras.common.ipnetwork.IpNetworkManagerException;

public interface IIpHostSpi extends IIpHost {

	IIpPort provisionPort(String type) throws IpNetworkManagerException;
	
	String getPrefixHost();

}
