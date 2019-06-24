package dev.voras.common.openstack.manager.internal;

import java.time.ZonedDateTime;
import java.util.ArrayList;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.voras.ICredentials;
import dev.voras.ICredentialsUsernamePassword;
import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.common.openstack.manager.internal.json.Api;
import dev.voras.common.openstack.manager.internal.json.Auth;
import dev.voras.common.openstack.manager.internal.json.AuthTokenResponse;
import dev.voras.common.openstack.manager.internal.json.AuthTokens;
import dev.voras.common.openstack.manager.internal.json.Domain;
import dev.voras.common.openstack.manager.internal.json.Endpoint;
import dev.voras.common.openstack.manager.internal.json.Identity;
import dev.voras.common.openstack.manager.internal.json.Password;
import dev.voras.common.openstack.manager.internal.json.Project;
import dev.voras.common.openstack.manager.internal.json.Scope;
import dev.voras.common.openstack.manager.internal.json.Server;
import dev.voras.common.openstack.manager.internal.json.ServerResponse;
import dev.voras.common.openstack.manager.internal.json.ServersResponse;
import dev.voras.common.openstack.manager.internal.json.User;
import dev.voras.framework.spi.IFramework;

public class OpenstackHttpClient {
	
	private final static Log logger = LogFactory.getLog(OpenstackHttpClient.class);

	private final OpenstackProperties     openstackProperties;
	private final IFramework              framework;
	
	private final CloseableHttpClient     httpClient;
	private OpenstackToken                openstackToken;

	public String openstackImageUri;
	public String openstackComputeUri;
	public String openstackNetworkUri;

	private Gson                          gson = new GsonBuilder().setPrettyPrinting().create();

	protected OpenstackHttpClient(IFramework framework, OpenstackProperties openstackProperties) {
		this.framework           = framework;
		this.openstackProperties = openstackProperties;
		this.httpClient          = HttpClients.createDefault();
	}
	
	protected void checkToken() throws OpenstackManagerException {
		if (openstackToken == null || !openstackToken.isOk()) {
			if (!connectToOpenstack()) {
				throw new OpenstackManagerException("Unable to re-authenticate with the OpenStack server");
			}
		}
	}

	private boolean connectToOpenstack() throws OpenstackManagerException {
		try {
			String credentialsId = this.openstackProperties.getCredentialsId();

			ICredentials credentials = null;
			try {
				credentials = framework.getCredentialsService().getCredentials(credentialsId);
			} catch(Exception e) {
				logger.warn("OpenStack is not available due to missing credentials " + credentialsId);
				return false;
			}

			if (!(credentials instanceof ICredentialsUsernamePassword)) {
				logger.warn("OpenStack credentials are not a username/password");
				return false;
			}

			ICredentialsUsernamePassword usernamePassword = (ICredentialsUsernamePassword) credentials;

			String identityEndpoint = this.openstackProperties.getServerIdentityUri();
			String domain = this.openstackProperties.getServerIdentityDomain();
			String project = this.openstackProperties.getServerIdentityProject();

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

	public Server findServerByName(@NotNull String serverName) throws OpenstackManagerException {
		try {
			checkToken();

			//*** Retrieve a list of the networks available and select one

			HttpGet get = new HttpGet(this.openstackComputeUri + "/servers");
			get.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new OpenstackManagerException("OpenStack list servers failed - " + status);
				}

				ServersResponse servers = this.gson.fromJson(entity, ServersResponse.class);
				if (servers != null && servers.servers != null) {
					for(Server server : servers.servers) {
						if (serverName.equals(server.name)) {
							return server;
						}
					}
				}
			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to list servers ", e);
		}
	}

	public void deleteServer(Server server) throws OpenstackManagerException {
		if (server.id == null) {
			return;
		}
		
		try {
			checkToken();

			//*** Retrieve a list of the networks available and select one

			HttpDelete delete = new HttpDelete(this.openstackComputeUri + "/servers/" + server.id);
			delete.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(delete)) {
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

	public Server getServer(String id) throws OpenstackManagerException {
		if (id == null) {
			return null;
		}
		
		try {
			checkToken();

			//*** Retrieve a list of the networks available and select one

			HttpGet get = new HttpGet(this.openstackComputeUri + "/servers/" + id);
			get.addHeader(this.openstackToken.getHeader());

			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());
				
				if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					return null;
				}

				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new OpenstackManagerException("OpenStack list servers failed - " + status);
				}

				ServerResponse server = this.gson.fromJson(entity, ServerResponse.class);
				if (server != null && server.server != null) {
					return server.server;
				}
			}
			return null;
		} catch(OpenstackManagerException e) {
			throw e;
		} catch(Exception e) {
			throw new OpenstackManagerException("Unable to list servers ", e);
		}
	}



}
