package dev.voras.common.openstack.manager.internal;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.voras.ICredentials;
import dev.voras.ICredentialsUsernamePassword;
import dev.voras.ManagerException;
import dev.voras.common.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.voras.common.linux.LinuxManagerException;
import dev.voras.common.linux.OperatingSystem;
import dev.voras.common.linux.spi.ILinuxManagerSpi;
import dev.voras.common.linux.spi.ILinuxProvisionedImage;
import dev.voras.common.linux.spi.ILinuxProvisioner;
import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.common.openstack.manager.internal.json.Api;
import dev.voras.common.openstack.manager.internal.json.Auth;
import dev.voras.common.openstack.manager.internal.json.AuthTokenResponse;
import dev.voras.common.openstack.manager.internal.json.AuthTokens;
import dev.voras.common.openstack.manager.internal.json.Domain;
import dev.voras.common.openstack.manager.internal.json.Endpoint;
import dev.voras.common.openstack.manager.internal.json.Flavor;
import dev.voras.common.openstack.manager.internal.json.Flavors;
import dev.voras.common.openstack.manager.internal.json.Floatingip;
import dev.voras.common.openstack.manager.internal.json.FloatingipRequestResponse;
import dev.voras.common.openstack.manager.internal.json.Identity;
import dev.voras.common.openstack.manager.internal.json.Image;
import dev.voras.common.openstack.manager.internal.json.Images;
import dev.voras.common.openstack.manager.internal.json.Network;
import dev.voras.common.openstack.manager.internal.json.Networks;
import dev.voras.common.openstack.manager.internal.json.Password;
import dev.voras.common.openstack.manager.internal.json.Port;
import dev.voras.common.openstack.manager.internal.json.PortsResponse;
import dev.voras.common.openstack.manager.internal.json.Project;
import dev.voras.common.openstack.manager.internal.json.Scope;
import dev.voras.common.openstack.manager.internal.json.Server;
import dev.voras.common.openstack.manager.internal.json.User;
import dev.voras.common.openstack.manager.internal.properties.LinuxImageCapabilities;
import dev.voras.common.openstack.manager.internal.properties.LinuxImages;
import dev.voras.common.openstack.manager.internal.properties.MaximumInstances;
import dev.voras.common.openstack.manager.internal.properties.NamePool;
import dev.voras.common.openstack.manager.internal.properties.OpenStackCredentialsId;
import dev.voras.common.openstack.manager.internal.properties.OpenStackDomainName;
import dev.voras.common.openstack.manager.internal.properties.OpenStackIdentityUri;
import dev.voras.common.openstack.manager.internal.properties.OpenStackProjectName;
import dev.voras.common.openstack.manager.internal.properties.OpenstackPropertiesSingleton;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.DynamicStatusStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IManager;
import dev.voras.framework.spi.IResourcePoolingService;
import dev.voras.framework.spi.InsufficientResourcesAvailableException;
import dev.voras.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class OpenstackManagerImpl extends AbstractManager implements ILinuxProvisioner {
	protected final static String NAMESPACE = "openstack";

	private final static Log logger = LogFactory.getLog(OpenstackManagerImpl.class);

	private IDynamicStatusStoreService dss;
	private IIpNetworkManagerSpi ipManager;
	private ILinuxManagerSpi     linuxManager;

	private final ArrayList<OpenstackLinuxImageImpl> instances = new ArrayList<>();

	private CloseableHttpClient           httpClient;
	private OpenstackToken                openstackToken;

	public String openstackImageUri;
	public String openstackComputeUri;
	public String openstackNetworkUri;

	private Gson                          gson = new GsonBuilder().setPrettyPrinting().create();


	/* (non-Javadoc)
	 * @see dev.voras.framework.spi.AbstractManager#initialise(dev.voras.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);

		//*** If this bundle was loaded, it means it was specifically requested.
		//*** Therefore mark as youAreRequired and if linux is present register as a provisioner
		youAreRequired(allManagers, activeManagers);

		try {
			this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
			OpenstackPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
		} catch (Exception e) {
			throw new LinuxManagerException("Unable to request framework services", e);
		}

		this.httpClient = HttpClients.createDefault();
	}

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);

		//*** Absolutely need the IP Network manager
		this.ipManager = addDependentManager(allManagers, activeManagers, IIpNetworkManagerSpi.class);
		if (this.ipManager == null) {
			throw new LinuxManagerException("The IP Network Manager is not available");
		}

		//*** Check if Linux is loaded
		this.linuxManager = addDependentManager(allManagers, activeManagers, ILinuxManagerSpi.class);
		if (this.linuxManager != null) {
			this.linuxManager.registerProvisioner(this);
		}

	}

	@Override
	public void provisionBuild() throws ManagerException, ResourceUnavailableException {
		for(OpenstackLinuxImageImpl instance : instances) {
			try {
				instance.generate();
			} catch(ConfigurationPropertyStoreException e) {
				throw new OpenstackManagerException("Problem building OpenStack servers", e);
			}
		}
	}


	@Override
	public void provisionDiscard() {

		for(OpenstackLinuxImageImpl instance : instances) {
			instance.discard();
		}

		if (this.httpClient != null) {
			try {
				this.httpClient.close();
			} catch (IOException e) { // Ignore error, not much we can do
			}
		}
	}





	@Override
	public ILinuxProvisionedImage provision(String tag, OperatingSystem operatingSystem, List<String> capabilities) throws OpenstackManagerException {

		//*** Check that we can connect to openstack before we attempt to provision, if we can't end gracefully and give someone else a chance
		if (this.openstackToken == null) {
			if (!connectToOpenstack()) {
				return null;
			}
		}


		//*** Locate the possible images that are available for selection
		try {
			List<String> possibleImages = LinuxImages.get(operatingSystem, null);

			//*** Filter out those that don't have the necessary capabilities
			if (!capabilities.isEmpty()) {
				Iterator<String> imageIterator = possibleImages.iterator();
				imageSearch:
					while(imageIterator.hasNext()) {
						String image = imageIterator.next();
						List<String> imageCapabilities = LinuxImageCapabilities.get(image);
						for(String requestedCapability : capabilities) {
							if (!imageCapabilities.contains(requestedCapability)) {
								imageIterator.remove();
								continue imageSearch;
							}
						}
					}
			}

			//*** Are there any images left?  if not return gracefully as some other provisioner may be able to support it
			if (possibleImages.isEmpty()) {
				return null;
			}

			//*** Select the first image as they will be listed in preference order
			String selectedImage = possibleImages.get(0);


			//*** See if we have capacity for a new Instance on Openstack
			String instanceName = reserveInstance();

			if (instanceName == null) {
				//*** No room, return gracefully and allow someone else a chance
				return null;
			}

			//*** We have one,  return it
			OpenstackLinuxImageImpl instance = new OpenstackLinuxImageImpl(this, instanceName, selectedImage, tag);	
			this.instances.add(instance);

			logger.info("Reserved OpenStack Linux instance " + instanceName + " with image " + selectedImage + " for tag " + tag);

			return instance;			
		} catch(ConfigurationPropertyStoreException e) {
			throw new OpenstackManagerException("Problem accessing the CPS", e);
		} catch (DynamicStatusStoreException e) {
			throw new OpenstackManagerException("Problem accessing the DSS", e);
		} catch (InsufficientResourcesAvailableException e) {
			//*** We don't have any spare capacity, so return gracefully
			return null;
		} catch (InterruptedException e) {
			throw new OpenstackManagerException("Processing interrupted", e);
		}
	}

	private String reserveInstance() throws DynamicStatusStoreException, InterruptedException, InsufficientResourcesAvailableException, ConfigurationPropertyStoreException, OpenstackManagerException {

		//*** Get the current and maximum instances
		int maxInstances = MaximumInstances.get();

		int currentInstances = 0;

		String sCurrentInstances = this.dss.get("server.current");
		if (sCurrentInstances != null) {
			currentInstances = Integer.parseInt(sCurrentInstances);
		}


		//*** Is there room?
		if (maxInstances <= currentInstances) {
			return null;
		}

		//*** Reserve a instance
		currentInstances++;		
		if (!dss.putSwap("server.current", sCurrentInstances, Integer.toString(currentInstances))) {
			//*** The value of the current instances changed whilst this was running,  so we need to try again with the updated value
			Thread.sleep(200); //*** To avoid race conditions
			return reserveInstance();
		}		

		//*** Generate an Instance Name
		String runName = this.getFramework().getTestRunName();

		String actualInstanceName = null;

		List<String> instanceNamePool = NamePool.get();
		IResourcePoolingService poolingService = this.getFramework().getResourcePoolingService();

		ArrayList<String> exclude = new ArrayList<>();
		while(true) {
			List<String> possibleNames = poolingService.obtainResources(instanceNamePool, exclude, 10, 1, this.dss, "compute");
			for(String possibleName : possibleNames) {
				String instanceName = "compute." + possibleName;
				HashMap<String, String> otherProps = new HashMap<>();
				otherProps.put("run." + runName + "." + instanceName, "active");

				if (dss.putSwap(instanceName, null, runName, otherProps)) {
					actualInstanceName = possibleName;
					break;
				}
			}
			if (actualInstanceName != null) {
				break;
			}
		}

		//*** we have a new Instance Name, so return 
		return actualInstanceName;
	}

	public IDynamicStatusStoreService getDSS() {
		return this.dss;
	}


	private boolean connectToOpenstack() throws OpenstackManagerException {
		try {
			String credentialsId = OpenStackCredentialsId.get();

			ICredentials credentials = null;
			try {
				credentials = getFramework().getCredentialsService().getCredentials(credentialsId);
			} catch(Exception e) {
				logger.warn("OpenStack is not available due to missing credentials " + credentialsId);
				return false;
			}

			if (!(credentials instanceof ICredentialsUsernamePassword)) {
				logger.warn("OpenStack credentials are not a username/password");
				return false;
			}

			ICredentialsUsernamePassword usernamePassword = (ICredentialsUsernamePassword) credentials;

			String identityEndpoint = OpenStackIdentityUri.get();
			String domain = OpenStackDomainName.get();
			String project = OpenStackProjectName.get();

			if (identityEndpoint == null || domain == null || project == null) {
				logger.warn("Openstack is unavailable due to identity, domain or project is missing in CPS");
				return false;
			}

			AuthTokens authTokens = new AuthTokens();
			authTokens.auth = new Auth();
			authTokens.auth.identity = new Identity();
			authTokens.auth.identity.methods = new ArrayList<>();
			authTokens.auth.identity.methods.add("password");
			authTokens.auth.identity.password = new Password();
			authTokens.auth.identity.password.user = new User();
			authTokens.auth.identity.password.user.name = usernamePassword.getUsername();
			authTokens.auth.identity.password.user.password = usernamePassword.getPassword();
			authTokens.auth.identity.password.user.domain = new Domain();
			authTokens.auth.identity.password.user.domain.name = domain;
			authTokens.auth.scope = new Scope();
			authTokens.auth.scope.project = new Project();
			authTokens.auth.scope.project.name = project;
			authTokens.auth.scope.project.domain = new Domain();
			authTokens.auth.scope.project.domain.name = domain;

			String content = gson.toJson(authTokens);

			HttpPost post = new HttpPost(identityEndpoint + "/auth/tokens");
			StringEntity entity = new StringEntity(content, ContentType.APPLICATION_JSON);
			post.setEntity(entity);

			try (CloseableHttpResponse response = this.httpClient.execute(post)) {
				StatusLine status = response.getStatusLine();
				HttpEntity responseEntity = response.getEntity();
				String responseString = EntityUtils.toString(responseEntity);
				if (status.getStatusCode() != HttpStatus.SC_CREATED) {
					logger.warn("OpenStack is not available due to identity responding with " + status);
					return false;
				}

				AuthTokenResponse tokenResponse = gson.fromJson(responseString, AuthTokenResponse.class);
				Header tokenHeader = response.getFirstHeader("X-Subject-Token");
				if (tokenHeader == null) {
					logger.warn("OpenStack is not available due to missing X-Subject-Token");
					return false;
				}

				this.openstackImageUri = null;

				if (tokenResponse.token != null && tokenResponse.token.catalog != null) {
					for(Api api : tokenResponse.token.catalog) {
						if ("image".equals(api.type)) {
							if (api.endpoints != null) {
								for(Endpoint endpoint : api.endpoints) {
									if ("public".equals(endpoint.endpoint_interface)) {
										this.openstackImageUri = endpoint.url;
									}
								}
							}
						} else if ("compute".equals(api.type)) {
							if (api.endpoints != null) {
								for(Endpoint endpoint : api.endpoints) {
									if ("public".equals(endpoint.endpoint_interface)) {
										this.openstackComputeUri = endpoint.url;
									}
								}
							}
						} else if ("network".equals(api.type)) {
							if (api.endpoints != null) {
								for(Endpoint endpoint : api.endpoints) {
									if ("public".equals(endpoint.endpoint_interface)) {
										this.openstackNetworkUri = endpoint.url;
									}
								}
							}
						}

					}
				}


				if (this.openstackImageUri == null) {
					logger.info("OpenStack is not available as some APIs are missing");
					return false;
				}


				String tokenString = tokenHeader.getValue();
				ZonedDateTime zdt = ZonedDateTime.parse(tokenResponse.token.expires_at);

				this.openstackToken = new OpenstackToken(tokenString, zdt.toInstant());

				return true;
			}
		} catch(Exception e) {
			logger.warn("OpenStack is not available due to " + e.getMessage()); // not reporting full stacktrace to keep log compact, as this could be expected
			return false;
		}
	}

	protected void checkToken() throws OpenstackManagerException {
		if (!openstackToken.isOk()) {
			if (!connectToOpenstack()) {
				throw new OpenstackManagerException("Unable to re-authenticate with the OpenStack server");
			}
		}
	}


	protected String getImageId(@NotNull String image) throws OpenstackManagerException {
		try {
			checkToken();

			//*** Retrieve a list of the images

			HttpGet get = new HttpGet(this.openstackImageUri + "/v2.5/images");
			get.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new OpenstackManagerException("OpenStack list image failed - " + status);
				}

				Images images = gson.fromJson(entity, Images.class);
				if (images != null && images.images != null) {
					for(Image i : images.images) {
						if (i.name != null) {
							if (image.equals(i.name)) {
								return i.id;
							}
						}
					}
				}
			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to list image " + image, e);
		}
	}

	protected String getFlavourId(@NotNull String flavour) throws OpenstackManagerException {
		try {
			checkToken();

			//*** Retrieve a list of the images

			HttpGet get = new HttpGet(this.openstackComputeUri + "/flavors");
			get.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new OpenstackManagerException("OpenStack list image failed - " + status);
				}

				Flavors flavours = gson.fromJson(entity, Flavors.class);
				if (flavours != null && flavours.flavors != null) {
					for(Flavor f : flavours.flavors) {
						if (f.name != null) {
							if (flavour.equals(f.name)) {
								return f.id;
							}
						}
					}
				}
			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to list flavour " + flavour, e);
		}
	}

	public Floatingip allocateFloatingip(Port port, Network network) throws OpenstackManagerException {
		try {
			checkToken();

			Floatingip fip = new Floatingip();
			fip.port_id    = port.id;
			fip.floating_network_id = network.id;
			fip.description = "voras_run=" + getFramework().getTestRunName();

			FloatingipRequestResponse fipRequest = new FloatingipRequestResponse();
			fipRequest.floatingip = fip;

			//*** Allocate a floating ip

			HttpPost post = new HttpPost(this.openstackNetworkUri + "/v2.0/floatingips");
			post.addHeader(this.openstackToken.getHeader());
			post.setEntity(new StringEntity(this.gson.toJson(fipRequest), ContentType.APPLICATION_JSON));

			try (CloseableHttpResponse response = httpClient.execute(post)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_CREATED) {
					throw new OpenstackManagerException("OpenStack list image failed - " + status);
				}

				FloatingipRequestResponse fipResponse = this.gson.fromJson(entity, FloatingipRequestResponse.class);
				if (fipResponse != null && fipResponse.floatingip != null) {
					return fipResponse.floatingip;
				}
			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to list floating ips ", e);
		}
	}

	public Network findExternalNetwork(String externalNetwork) throws OpenstackManagerException {
		try {
			checkToken();

			//*** Retrieve a list of the networks available and select one

			HttpGet get = new HttpGet(this.openstackNetworkUri + "/v2.0/networks");
			get.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new OpenstackManagerException("OpenStack list networks failed - " + status);
				}
				System.out.println(entity);

				Networks networks = this.gson.fromJson(entity, Networks.class);
				if (networks != null && networks.networks != null) {
					for(Network network : networks.networks) {
						if (externalNetwork != null && externalNetwork.equals(network.name)) {
							return network;
						} else {
							if (network.route_external) {
								return network;
							}
						}
					}
				}


			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to list floating ips ", e);
		}
	}

	public Port retrievePort(@NotNull String deviceId) throws OpenstackManagerException {
		try {
			checkToken();

			//*** Retrieve all the ports and extract the correct one

			HttpGet get = new HttpGet(this.openstackNetworkUri + "/v2.0/ports");
			get.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new OpenstackManagerException("OpenStack list port failed - " + status);
				}

				PortsResponse portsResponse = this.gson.fromJson(entity, PortsResponse.class);
				if (portsResponse != null && portsResponse.ports != null) {
					for(Port port : portsResponse.ports) {
						if (deviceId.equals(port.device_id)) {
							return port;
						}
					}
				}
			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to retrieve the server port", e);
		}
	}

	public String retrieveServerPassword(@NotNull Server server) throws OpenstackManagerException {
		try {
			checkToken();

			//*** Retrieve all the ports and extract the correct one

			HttpGet get = new HttpGet(this.openstackComputeUri + "/servers/" + server.id + "/os-server-password");
			get.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());
				System.out.println(entity);
				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new OpenstackManagerException("OpenStack list os password failed - " + status);
				}

			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to retrieve the server os password", e);
		}
	}

	public void deleteFloatingip(Floatingip openstackFloatingip) throws OpenstackManagerException {
		try { 
			HttpDelete post = new HttpDelete(this.openstackNetworkUri + "/v2.0/floatingips/" + openstackFloatingip.id);
			post.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(post)) {
				StatusLine status = response.getStatusLine();
				EntityUtils.consume(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
					throw new OpenstackManagerException("OpenStack delete floatingip failed - " + status);
				}
			}
			return;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to delete floating ip ", e);
		}
	}
	
	
	public void deleteServer(Server openstackServer) throws OpenstackManagerException {
		try { 
			HttpDelete post = new HttpDelete(this.openstackNetworkUri + "/servers/" + openstackServer.id);
			post.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(post)) {
				StatusLine status = response.getStatusLine();
				EntityUtils.consume(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
					throw new OpenstackManagerException("OpenStack delete server failed - " + status);
				}
			}
			return;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to delete server ", e);
		}
	}



	public String getOpenStackComputeUri() {
		return this.openstackComputeUri;
	}

	public String getOpenStackNetworkUri() {
		return this.openstackComputeUri;
	}

	public CloseableHttpClient getHttpClient() {
		return this.httpClient;
	}

	public OpenstackToken getToken() {
		return this.openstackToken;
	}

	protected Gson getGson() {
		return this.gson;
	}

}
