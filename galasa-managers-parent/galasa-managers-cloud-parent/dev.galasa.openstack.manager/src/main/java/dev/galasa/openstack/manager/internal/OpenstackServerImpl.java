/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.openstack.manager.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.openstack.manager.OpenstackManagerException;
import dev.galasa.openstack.manager.internal.json.Floatingip;
import dev.galasa.openstack.manager.internal.json.Server;

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
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.warn("Wait for server delete interrupted",e);
					return;
				}
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

		try {
			freeServerFromDss(serverName, runName, dss);
		} catch(Exception e) {
			logger.error("Cleanup of DSS failed",e);
		}
	}

	private static void freeServerFromDss(String serverName, String runName, IDynamicStatusStoreService dss) throws DynamicStatusStoreException, InterruptedException {
		String currentInstances = dss.get("server.current.compute.instances");

		int usedInstances = 0;
		if (currentInstances != null) {
			usedInstances = Integer.parseInt(currentInstances);
		}
		usedInstances--;
		if (usedInstances < 0) {
			usedInstances = 0;
		}

		//*** Remove the userview set
		// TODO create userview set

		//*** Remove the control set
		String prefix = "compute." + serverName;
		HashMap<String, String> otherProps = new HashMap<>();
		otherProps.put("run." + runName + "." + prefix, "free");
		if (!dss.putSwap("server.current.compute.instances", currentInstances, Integer.toString(usedInstances), otherProps)) {
			//*** The value of the current slots changed whilst this was running,  so we need to try again with the updated value
			Thread.sleep(200); //*** To avoid race conditions
			freeServerFromDss(prefix, prefix, dss);
			return;
		}

		//*** Clear the DSS for this run completely
		HashSet<String> deleteProperties = new HashSet<>();
		deleteProperties.add("run." + runName + "." + prefix);
		deleteProperties.add(prefix);
		dss.delete(deleteProperties);
	}

	public static void deleteFloatingIpByName(String fipName, 
			String runName, 
			IDynamicStatusStoreService dss, 
			OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {

		//*** Need to locate the id of the floatingup before we can delete it

		Floatingip fip = openstackHttpClient.findFloatingIpByName(fipName);

		//*** Now delete the floatingip,  doesn't matter if the id is null as we want the DSS stuff deleted anyway

		deleteFloatingIp(fip, fipName, runName, dss, openstackHttpClient);
	}

	public static void deleteFloatingIp(Floatingip floatingip, 
			String fipName,
			String runName, 
			IDynamicStatusStoreService dss,
			OpenstackHttpClient openstackHttpClient) throws OpenstackManagerException {

		if (floatingip != null && floatingip.id != null) {
			openstackHttpClient.deleteFloatingIp(floatingip);

			Instant expire = Instant.now();
			expire = expire.plus(1, ChronoUnit.MINUTES);  // TODO cps
			boolean deleted = false;
			while(expire.compareTo(Instant.now()) >= 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.warn("Wait for server delete interrupted",e);
					return;
				}
				Floatingip deletingFip = openstackHttpClient.getFloatingIp(floatingip.id);
				if (deletingFip == null) {
					deleted = true;
					break;
				}
			}

			if (!deleted) {
				logger.warn("Failed to delete an OpenStack floatingip in time - " + fipName + "/" + floatingip.id);
				return;
			}

			logger.info("Successfully deleted OpenStack floatingip " + fipName + "/" + floatingip.id);
		}

		try {
			freeFloatingipFromDss(fipName, runName, dss);
		} catch(Exception e) {
			logger.error("Cleanup of DSS failed",e);
		}
	}

	private static void freeFloatingipFromDss(String fipName, String runName, IDynamicStatusStoreService dss) throws DynamicStatusStoreException, InterruptedException {
		//*** Remove the userview set
		// TODO create userview set

		//*** Remove the control set
		String fipSub = fipName.replaceAll("\\.", "_");
		String prefix = "floatingip." + fipSub;

		//*** Clear the DSS for this run completely
		HashSet<String> deleteProperties = new HashSet<>();
		deleteProperties.add("run." + runName + "." + prefix);
		deleteProperties.add(prefix);
		dss.delete(deleteProperties);
	}
	
	protected static void registerFloatingIp(IDynamicStatusStoreService dss, String runName, Floatingip floatingIp) throws DynamicStatusStoreException {
		
		String fip = floatingIp.floating_ip_address.replaceAll("\\.", "_");
		
		String prefix = "floatingip." + fip;
		
		HashMap<String, String> fipProperties = new HashMap<>();
		fipProperties.put("run." + runName + "." + prefix, "active");
		fipProperties.put(prefix, runName);
		dss.put(fipProperties);
	}



}
