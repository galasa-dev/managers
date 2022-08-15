/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.internal;

import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.spi.AbstractGenericIpHost;
import dev.galasa.zos.ZosManagerException;

public class ZosIpHostImpl extends AbstractGenericIpHost {
	
	private ZosManagerImpl zosManager;
	private String image;
	
    public ZosIpHostImpl(ZosManagerImpl zosManager, String imageId) throws IpNetworkManagerException, CredentialsException {
        super(zosManager.getCPS(), zosManager.getDSS(), zosManager.getFramework().getCredentialsService(), "image", imageId);
        this.zosManager = zosManager;
        this.image = imageId;
    }

    @Override
    public IIpPort provisionPort(String type) throws IpNetworkManagerException {
		try {
			String allocatedPort = zosManager.getZosPortController().allocatePort(image);
			return new ZosIpPortImpl(this, Integer.parseInt(allocatedPort), type);
		} catch (ZosManagerException exception) {
			throw new IpNetworkManagerException("Exception whilst allocating a z/OS port from the pool");
		}
	}
}
