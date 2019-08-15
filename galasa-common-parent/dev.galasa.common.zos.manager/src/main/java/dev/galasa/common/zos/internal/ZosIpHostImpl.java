package dev.galasa.common.zos.internal;

import dev.galasa.common.ipnetwork.IIpPort;
import dev.galasa.common.ipnetwork.IpNetworkManagerException;
import dev.galasa.common.ipnetwork.spi.AbstractGenericIpHost;
import dev.galasa.framework.spi.creds.CredentialsException;

public class ZosIpHostImpl extends AbstractGenericIpHost {
	public ZosIpHostImpl(ZosManagerImpl zosManager, String imageId) throws IpNetworkManagerException, CredentialsException {
		super(zosManager.getCPS(), zosManager.getDSS(), zosManager.getFramework().getCredentialsService(), "image", imageId);
	}

	@Override
	public IIpPort provisionPort(String type) throws IpNetworkManagerException {
		throw new UnsupportedOperationException("Not written yet");
	}


}
