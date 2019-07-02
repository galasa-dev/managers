package dev.voras.common.openstack.manager.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.ICommandShell;
import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.linux.LinuxManagerException;
import dev.voras.common.linux.spi.ILinuxProvisionedImage;
import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.common.openstack.manager.internal.json.Floatingip;
import dev.voras.common.openstack.manager.internal.json.Network;
import dev.voras.common.openstack.manager.internal.json.Port;
import dev.voras.common.openstack.manager.internal.json.Server;
import dev.voras.common.openstack.manager.internal.json.ServerRequest;
import dev.voras.common.openstack.manager.internal.json.VorasMetadata;
import dev.voras.common.openstack.manager.internal.properties.GenerateTimeout;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;

public class OpenstackLinuxImageImpl extends OpenstackServerImpl implements ILinuxProvisionedImage {

	private static final Log logger = LogFactory.getLog(OpenstackLinuxImageImpl.class);

	public final OpenstackManagerImpl manager; 
	private final OpenstackHttpClient openstackHttpClient;
	public final String instanceName;
	public final String image;
	public final String tag;

	private String id;
	private String username;
	private String password;

	private Server openstackServer;
	private Port   openstackPort;
	private Floatingip openstackFloatingip;

	public OpenstackLinuxImageImpl(@NotNull OpenstackManagerImpl manager,
			@NotNull OpenstackHttpClient openstackHttpClient,
			@NotNull String instanceName, 
			@NotNull String image,
			@NotNull String tag) {
		this.manager             = manager;
		this.openstackHttpClient = openstackHttpClient;
		this.instanceName        = instanceName;
		this.image               = image;
		this.tag                 = tag;
	}


	@Override
	public @NotNull String getImageID() {
		return this.instanceName;
	}

	@Override
	public @NotNull IIpHost getIpHost() {
		return new OpenstackIpHost();
	}

	@Override
	public @NotNull ICredentials getDefaultCredentials() throws LinuxManagerException {
		return new OpenstackUsernamePasswordCredentials();
	}

	public void discard() {
		try {
			//*** TODO delete the instance in Openstack
			if (this.openstackServer != null) {
				try {
					deleteServer(this.openstackServer, 
							this.openstackServer.name, 
							this.manager.getFramework().getTestRunName(),
							this.manager.getDSS(),
							this.openstackHttpClient);
				} catch(Exception e) {
					logger.warn("Failed to delete the server",e);
				}
			}

			//*** Delete the Floating IP
			if (this.openstackFloatingip != null) {
				try {
					deleteFloatingIp(this.openstackFloatingip, 
							this.openstackFloatingip.floating_ip_address, 
							this.manager.getFramework().getTestRunName(),
							this.manager.getDSS(),
							this.openstackHttpClient);
				} catch(Exception e) {
					logger.warn("Failed to delete the floating ip",e);
				}
			}
			logger.info("OpenStack Linux instance " + this.instanceName + " for tag " + tag + " has been discarded");
		} catch(Exception e) {
			logger.warn("Unable to discard OpenStack Linux instance " + this.instanceName, e);
		}
	}


	public void generate() throws OpenstackManagerException, ConfigurationPropertyStoreException {
		logger.info("Generating OpenStack Linux instance " + this.instanceName + " with image " + this.image + " for tag " + this.tag);

		String flavor = "m1.small";
		int generateTimeout = GenerateTimeout.get(); 
		generateTimeout = 1;

		Server server = new Server();
		server.name = this.instanceName;
		server.imageRef = this.openstackHttpClient.getImageId(this.image);
		server.flavorRef = this.openstackHttpClient.getFlavourId(flavor);
		server.availability_zone = "nova"; // TODO cps
		server.metadata = new VorasMetadata();
		server.metadata.voras_run = this.manager.getFramework().getTestRunName();
		server.key_name = "voras"; // TODO cps

		if (server.imageRef == null) {
			throw new OpenstackManagerException("Image " + this.image + " is missing in OpenStack");
		}

		if (server.flavorRef == null) {
			throw new OpenstackManagerException("Flavor " + flavor + " is missing in OpenStack");
		}



		ServerRequest serverRequest = new ServerRequest();
		serverRequest.server = server;

		try {
			this.openstackServer = this.openstackHttpClient.createServer(serverRequest);
			this.id = this.openstackServer.id;
			this.password = this.openstackServer.adminPass;
			
			Instant expire = Instant.now();
			expire = expire.plus(generateTimeout, ChronoUnit.MINUTES);

			String serverJson = "";
			String state = null;
			while(expire.compareTo(Instant.now()) > 0) {
				Thread.sleep(5000);

				Server checkServer = this.openstackHttpClient.getServer(this.id);
				if (checkServer != null) {
					if (checkServer.power_state != null) {
						if (checkServer.power_state == 1) {
							logger.info("OpenStack Linux instance " + this.instanceName + " is running");
							this.openstackServer = checkServer;
							break;
						}
						state = checkServer.task_state;
					}
				}

				logger.trace("Still waiting for OpenStack Linux instance " + this.instanceName + " to be built, task=" + state);  // TODO switch to trace
			}

			if (this.openstackServer == null) {
				throw new OpenstackManagerException("OpenStack failed to build the server in time, last response was:-\n" + serverJson);
			}

			//*** Get the network port details
			this.openstackPort = this.openstackHttpClient.retrievePort(this.openstackServer.id);
			if (this.openstackPort == null) {
				throw new OpenstackManagerException("OpenStack did not allocate a port for this instance");
			}

			//*** Locate the external network
			Network network = this.openstackHttpClient.findExternalNetwork(null);  //TODO provide means to specify network

			if (network == null) {
				throw new OpenstackManagerException("Unable to select an external network to allocate a floatingip on");
			}

			//*** Assign a floating IPv4 address
			this.openstackFloatingip = this.openstackHttpClient.allocateFloatingip(this.openstackPort, network);
			
			//*** Create the DSS properties to manager the Floating IP Address
			registerFloatingIp(this.manager.getDSS(), this.manager.getFramework().getTestRunName(), this.openstackFloatingip);

			//*** Assign a floating IPv4 address
			this.password = this.openstackHttpClient.retrieveServerPassword(this.openstackServer);
			
			System.out.println("done");

		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to start OpenStack Linux server", e);
		}

	}


	@Override
	public @NotNull ICommandShell getCommandShell() throws LinuxManagerException {
		// TODO Auto-generated method stub
		return null;
	}


}
