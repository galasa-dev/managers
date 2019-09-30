package dev.galasa.common.linux.internal;

import dev.galasa.common.ipnetwork.IIpPort;
import dev.galasa.common.ipnetwork.IpNetworkManagerException;
import dev.galasa.common.ipnetwork.spi.AbstractGenericIpHost;
import dev.galasa.framework.spi.creds.CredentialsException;

public class LinuxDSEIpHost extends AbstractGenericIpHost {
	
	protected LinuxDSEIpHost(LinuxManagerImpl linuxManager, String hostid) throws IpNetworkManagerException, CredentialsException {
		super(linuxManager.getCps(), linuxManager.getDss(), linuxManager.getFramework().getCredentialsService(), "image", hostid);
	}

	@Override
	public IIpPort provisionPort(String type) throws IpNetworkManagerException {
		throw new UnsupportedOperationException("Not written yet");
	}

}
