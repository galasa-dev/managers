package dev.voras.common.zos.internal;

import dev.voras.common.zos.ZosManagerException;

public class ZosDseImageImpl extends ZosBaseImageImpl {

	public ZosDseImageImpl(ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
		super(zosManager, imageId, clusterId);
	}

}
