package dev.galasa.common.ipnetwork.spi;

import dev.galasa.common.ipnetwork.IIpHost;
import dev.galasa.common.ipnetwork.IIpPort;
import dev.galasa.common.ipnetwork.IpNetworkManagerException;

public interface IIpHostSpi extends IIpHost {

	IIpPort provisionPort(String type) throws IpNetworkManagerException;
	
	String getPrefixHost();

}
