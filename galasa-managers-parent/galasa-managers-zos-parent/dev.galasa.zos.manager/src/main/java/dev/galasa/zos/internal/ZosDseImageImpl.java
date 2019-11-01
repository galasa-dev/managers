/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.internal;

import dev.galasa.zos.ZosManagerException;

public class ZosDseImageImpl extends ZosBaseImageImpl {

	public ZosDseImageImpl(ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
		super(zosManager, imageId, clusterId);
	}

}
