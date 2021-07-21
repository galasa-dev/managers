/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.IZosLibertyServer;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyImpl implements IZosLiberty {
	private static final Log logger = LogFactory.getLog(ZosLibertyImpl.class);

    private ZosLibertyManagerImpl zosLibertyManager;
    private List<IZosLibertyServer> libertyServers = new ArrayList<>();

    public ZosLibertyImpl(ZosLibertyManagerImpl zosLibertyManager) {
        this.zosLibertyManager = zosLibertyManager;
    }

    @Override
    public IZosLibertyServer newZosLibertyServer(IZosImage zosImage, String wlpInstallDir, String wlpUserDir, String wlpOutputDir) throws ZosLibertyServerException {
    	ZosLibertyServerImpl libertyServer = new ZosLibertyServerImpl(this, zosImage, wlpInstallDir, wlpUserDir, wlpOutputDir);
    	libertyServers.add(libertyServer);
        return libertyServer;
    }

    @Override
    public IZosLibertyServer newZosLibertyServer(IZosImage zosImage) throws ZosLibertyServerException {
    	ZosLibertyServerImpl libertyServer = new ZosLibertyServerImpl(this, zosImage, null, null, null);
    	libertyServers.add(libertyServer);
        return libertyServer;
    }

    protected ZosLibertyManagerImpl getZosLibertyManager() throws ZosLibertyManagerException {
        return this.zosLibertyManager;
    }
    
    protected void cleanup(boolean endOfTestRun) {
        if (endOfTestRun) {
        	for (IZosLibertyServer libertyServer : libertyServers) {
        		try {
					libertyServer.saveToResultsArchive();
				} catch (ZosLibertyServerException e) {
					logger.info("Problem archiving Liberty server", e);
				}
        		try {
					libertyServer.delete();
				} catch (ZosLibertyServerException e) {
					logger.info("Problem deleting Liberty server", e);
				}
        	}
        }
    }
}
