package dev.voras.common.openstack.manager.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.linux.LinuxManagerException;
import dev.voras.common.linux.spi.ILinuxProvisionedImage;
import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.common.openstack.manager.internal.json.Floatingip;
import dev.voras.common.openstack.manager.internal.json.Network;
import dev.voras.common.openstack.manager.internal.json.Port;
import dev.voras.common.openstack.manager.internal.json.Server;
import dev.voras.common.openstack.manager.internal.json.ServerRequest;
import dev.voras.common.openstack.manager.internal.json.ServerResponse;
import dev.voras.common.openstack.manager.internal.json.VorasMetadata;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.DynamicStatusStoreException;
import dev.voras.framework.spi.IDynamicStatusStoreService;

public class OpenstackLinuxImageImpl implements ILinuxProvisionedImage {

	private static final Log logger = LogFactory.getLog(OpenstackLinuxImageImpl.class);

	public final OpenstackManagerImpl manager; 
	public final String instanceName;
	public final String image;
	public final String tag;

	public String id;
	public String username;
	public String password;

	public Server openstackServer;
	public Port   openstackPort;
	public Floatingip openstackFloatingip;

	public OpenstackLinuxImageImpl(@NotNull OpenstackManagerImpl manager,
			@NotNull String instanceName, 
			@NotNull String image,
			@NotNull String tag) {
		this.manager      = manager;
		this.instanceName = instanceName;
		this.image        = image;
		this.tag          = tag;
	}


	@Override
	public @NotNull String getImageID() {
		return this.instanceName;
	}

	@Override
	public @NotNull IIpHost getIpHost() {
		return null;
	}

	@Override
	public @NotNull ICredentials getDefaultCredentials() throws LinuxManagerException {
		return null;
	}

	public void discard() {
		try {
			boolean error = false;
			
			//*** TODO delete the instance in Openstack
			if (this.openstackServer != null) {
				try {
					this.manager.deleteServer(this.openstackServer);
				} catch(Exception e) {
					logger.warn("Failed to delete the server",e);
				}
			}

			//*** Delete the Floating IP
			if (this.openstackFloatingip != null) {
				try {
					this.manager.deleteFloatingip(this.openstackFloatingip);
				} catch(Exception e) {
					logger.warn("Failed to delete the floating ip",e);
				}
			}





			logger.info("OpenStack Linux instance " + this.instanceName + " for tag " + tag + " has been discarded");

			//*** Now free the instance in DSS if everything was deleted
			if (!error) {
				freeInstance();
			}
		} catch(Exception e) {
			logger.warn("Unable to discard OpenStack Linux instance " + this.instanceName, e);
		}
	}


	private void freeInstance() throws DynamicStatusStoreException, InterruptedException {
		int currentInstances = 0;

		IDynamicStatusStoreService dss = manager.getDSS();

		String sCurrentInstances = dss.get("instances.current");
		if (sCurrentInstances != null) {
			currentInstances = Integer.parseInt(sCurrentInstances);
		}

		//*** Release an instance
		currentInstances--;
		if (currentInstances < 0) {
			currentInstances = 0;
		}

		String name = "instance." + this.instanceName;
		String runName = manager.getFramework().getTestRunName();

		HashMap<String, String> otherProps = new HashMap<>();
		otherProps.put("run." + runName + "." + name, "free");

		if (!dss.putSwap("instances.current", sCurrentInstances, Integer.toString(currentInstances), otherProps)) {
			//*** The value of the current instances changed whilst this was running,  so we need to try again with the updated value
			Thread.sleep(200); //*** To avoid race conditions
			freeInstance();
			return;
		}	

		//*** Got it,  delete the remaining properties
		HashSet<String> otherDeletes = new HashSet<>();
		otherDeletes.add("run." + runName + "." + name);
		otherDeletes.add(name);
		dss.delete(otherDeletes);
	}


	public void generate() throws OpenstackManagerException, ConfigurationPropertyStoreException {
		logger.info("Generating OpenStack Linux instance " + this.instanceName + " with image " + this.image + " for tag " + this.tag);

		this.manager.checkToken();

		Gson gson = this.manager.getGson();

		String flavor = "m1.small";
		int generateTimeout = this.manager.getProperties().getTimeout(); 
		generateTimeout = 1;

		Server server = new Server();
		server.name = this.instanceName;
		server.imageRef = this.manager.getImageId(this.image);
		server.flavorRef = this.manager.getFlavourId(flavor);
		server.availability_zone = "nova"; // TODO cps
		server.metadata = new VorasMetadata();
		server.metadata.voras_run = this.manager.getFramework().getTestRunName();
		server.adminPass = "C9L8k7SvQwZR"; // TODO cps
		server.key_name = "Michael Baylis"; // TODO cps

		if (server.imageRef == null) {
			throw new OpenstackManagerException("Image " + this.image + " is missing in OpenStack");
		}

		if (server.flavorRef == null) {
			throw new OpenstackManagerException("Flavor " + flavor + " is missing in OpenStack");
		}



		ServerRequest serverRequest = new ServerRequest();
		serverRequest.server = server;

		try {
			HttpPost get = new HttpPost(this.manager.getOpenStackComputeUri() + "/servers");
			get.addHeader(this.manager.getToken().getHeader());
			get.setEntity(new StringEntity(gson.toJson(serverRequest)));

			try (CloseableHttpResponse response = this.manager.getHttpClient().execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_ACCEPTED) {
					throw new OpenstackManagerException("OpenStack create server failed - " + status + "\n" + entity);
				}

				ServerResponse serverResponse = gson.fromJson(entity, ServerResponse.class);
				if (serverResponse.server != null) {
					this.password = serverResponse.server.adminPass;
					this.id     = serverResponse.server.id;
				}

				if (this.id == null) {
					throw new OpenstackManagerException("OpenStack did not return a server id");
				}

				if (this.password == null) {
					throw new OpenstackManagerException("OpenStack did not return a password");
				}
			}

			Instant expire = Instant.now();
			expire = expire.plus(generateTimeout, ChronoUnit.MINUTES);

			String serverJson = "";
			String state = null;
			while(expire.compareTo(Instant.now()) > 0) {
				Thread.sleep(5000);

				HttpGet statusCheck = new HttpGet(this.manager.getOpenStackComputeUri() + "/servers/" + this.id);
				statusCheck.addHeader(this.manager.getToken().getHeader());
				try (CloseableHttpResponse response = this.manager.getHttpClient().execute(statusCheck)) {
					StatusLine status = response.getStatusLine();
					serverJson = EntityUtils.toString(response.getEntity());

					System.out.println(serverJson);

					if (status.getStatusCode() != HttpStatus.SC_OK) {
						throw new OpenstackManagerException("OpenStack check server failed - " + status + "\n" + serverJson);
					}

					ServerResponse serverResponse = gson.fromJson(serverJson, ServerResponse.class);
					if (serverResponse != null && serverResponse.server != null && serverResponse.server.power_state != null) {
						if (serverResponse.server.power_state == 1) {
							logger.info("OpenStack Linux instance " + this.instanceName + " is running");
							this.openstackServer = serverResponse.server;
							break;
						}
						state = serverResponse.server.task_state;
					}
				}

				logger.trace("Still waiting for OpenStack Linux instance " + this.instanceName + " to be built, task=" + state);  // TODO switch to trace
			}

			if (this.openstackServer == null) {
				throw new OpenstackManagerException("OpenStack failed to build the server in time, last response was:-\n" + serverJson);
			}

			//*** Get the network port details
			this.openstackPort = this.manager.retrievePort(this.openstackServer.id);
			if (this.openstackPort == null) {
				throw new OpenstackManagerException("OpenStack did not allocate a port for this instance");
			}

			//*** Locate the external network
			Network network = this.manager.findExternalNetwork(null);  //TODO provide means to specify network

			if (network == null) {
				throw new OpenstackManagerException("Unable to select an external network to allocate a floatingip on");
			}

			//*** Assign a floating IPv4 address
			this.openstackFloatingip = this.manager.allocateFloatingip(this.openstackPort, network);

			System.out.println("done");

		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to start OpenStack Linux server", e);
		}

	}

}
