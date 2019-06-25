package dev.voras.common.openstack.manager.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.common.openstack.manager.internal.json.Server;
import dev.voras.framework.spi.IDynamicStatusStoreService;

public class OpenstackServerImpl {
	
	private final static Log logger = LogFactory.getLog(OpenstackServerImpl.class);

	public static void deleteServerByName(String serverName, 
			String runName, 
			IDynamicStatusStoreService dss, 
			OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {
		
		//*** Need to locate the id of the server before we can delete it
		
		Server server = openstackHttpClient.findServerByName(serverName);
		
		//*** Now delete the server,  doesn't matter if the id is null as we want the DSS stuff deleted anyway
		
		deleteServer(server, serverName, runName, dss, openstackHttpClient);
	}
	
	public static void deleteServer(Server server,
			String serverName,
			String runName, 
			IDynamicStatusStoreService dss, 
			OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {
		
		if (server != null && server.id != null) {
			openstackHttpClient.deleteServer(server);
			
			Instant expire = Instant.now();
			expire = expire.plus(1, ChronoUnit.MINUTES);  // TODO cps
			boolean deleted = false;
			while(expire.compareTo(Instant.now()) >= 0) {
				Server deletingServer = openstackHttpClient.getServer(server.id);
				if (deletingServer == null) {
					deleted = true;
					break;
				}
			}
			
			if (!deleted) {
				logger.warn("Failed to delete an OpenStack compute server in time - " + serverName + "/" + server.id);
				return;
			}
			
			logger.info("Successfully deleted OpenStack compute server " + serverName + "/" + server.id);
		}
		
		freeServerFromDss(serverName, runName, dss);
		
		
	}

	private static void freeServerFromDss(String serverName, String runName, IDynamicStatusStoreService dss) {
		// TODO Auto-generated method stub
		
	}

}
