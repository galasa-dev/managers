package dev.galasa.zosliberty.internal;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
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
	public IZosLibertyServer newZosLibertyServer(IZosImage zosImage, IZosUNIXFile wlpInstallDir, IZosUNIXFile wlpUserDir) throws ZosLibertyServerException {
		return new ZosLibertyServerImpl(this, zosImage, wlpInstallDir, wlpUserDir);
	}

	@Override
	public IZosLibertyServer newZosLibertyServer() throws ZosLibertyServerException {
		return null;
	}

	@Override
	public void saveToResultsArchive(String rasPath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteLogs() {
		// TODO Auto-generated method stub
		
	}

	protected IZosFileHandler getZosFileHandler() throws ZosLibertyManagerException {
		return this.zosLibertyManager.getZosFileHandler();
	}

}
