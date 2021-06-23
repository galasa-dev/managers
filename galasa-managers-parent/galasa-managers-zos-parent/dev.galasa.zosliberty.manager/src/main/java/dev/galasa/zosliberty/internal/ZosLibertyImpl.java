/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty.internal;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.IZosLibertyServer;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyImpl implements IZosLiberty {

	private ZosLibertyManagerImpl zosLibertyManager;

	public ZosLibertyImpl(ZosLibertyManagerImpl zosLibertyManager) {
		this.zosLibertyManager = zosLibertyManager;
	}

	@Override
	public IZosLibertyServer newZosLibertyServer(IZosImage zosImage, IZosUNIXFile wlpInstallDir, IZosUNIXFile wlpUserDir, IZosUNIXFile wlpOutputDir) throws ZosLibertyServerException {
		return  new ZosLibertyServerImpl(this, zosImage, wlpInstallDir, wlpUserDir, wlpOutputDir);
	}

	@Override
	public IZosLibertyServer newZosLibertyServer() throws ZosLibertyServerException {
		//TODO: Create server
		throw new ZosLibertyServerException("Method not implemented");
	}

	protected ZosLibertyManagerImpl getZosLibertyManager() throws ZosLibertyManagerException {
		return this.zosLibertyManager;
	}

}
