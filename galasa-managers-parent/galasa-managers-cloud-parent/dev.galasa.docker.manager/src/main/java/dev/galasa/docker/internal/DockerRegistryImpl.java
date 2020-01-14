package dev.galasa.docker.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.ConcurrentModificationException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;

/**
 * Docker RegistryImpl. Controls the location of where docker images can be pulled from
 * 
 * @author James Davies
 */
public class DockerRegistryImpl {
    private IFramework                      framework;
    private DockerManagerImpl               dockerManager;
    private URL                             registryUrl;

    private ICredentialsService				credService;
	private ICredentialsUsernamePassword 	creds; 

	private String							authToken;

	private IHttpClient 				client;
	private IHttpClient 				clientDockerAuth;

	private Gson                            gson = new Gson();

    private static final Log                logger = LogFactory.getLog(DockerRegistryImpl.class);

	/**
	 * Sets up the registry that the manager can use to pull images from.
	 * 
	 * @param framework
	 * @param dockerManager
	 * @param registryUrl
	 * @throws DockerManagerException
	 */
    public DockerRegistryImpl(IFramework framework,	DockerManagerImpl dockerManager, URL registryUrl) throws DockerManagerException {
        this.framework          = framework;
        this.dockerManager      = dockerManager;
		this.registryUrl        = registryUrl;
		

        this.client             = dockerManager.httpManager.newHttpClient();
        this.clientDockerAuth   = dockerManager.httpManager.newHttpClient();

        try {
			this.client.setURI(this.registryUrl.toURI());
			this.clientDockerAuth.setURI(new URI("https://auth.docker.io"));
			this.credService = framework.getCredentialsService();
		} catch (URISyntaxException e) {
			logger.error("Registry URL is incompatible", e);
			throw new DockerManagerException("Could not parse Docker registry URL.", e);
		} catch (CredentialsException e) {
			logger.error("Could not access credential store from framework.", e);
			throw new DockerManagerException("Could not access credential store from framework.",e);
		}

    }

	/**
	 * Checks the registry for and image.
	 * 
	 * @param namespace
	 * @param repository
	 * @param tag
	 * @return true/false
	 */
	public boolean doYouHave(String namespace, String repository, String tag) {
		String resp = null;
		try {
			registryAuthenticate(namespace, repository);
			
			//Artifactory repositories do not need a namespace
			if (namespace != "") {
				namespace += "/";
			}
			String path = "/v2/repositories/" + namespace + repository + "/tags/" + tag;

//			String sResponse = client.get(path);
			HttpClientResponse<JsonObject> response = client.getJson(path);
			if(response.getStatusCode() == (HttpStatus.SC_OK)){
				return true;
			}
			
			return false;
		} catch(HttpClientException e) {
			return false;
		} catch(IllegalStateException e) {
			return false;
		} catch(ClassCastException e) {
			logger.warn("Invalid JSON returned from Docker Registry\n" + resp , e);
			return false;
		}
	}

	private void generateDockerServerAuthStructure(String user, String password) {
		JsonObject creds = new JsonObject();
		creds.addProperty("username", user);
		creds.addProperty("password", password);
	
		this.authToken = Base64.getEncoder().encodeToString(creds.toString().getBytes());
	}

	/**
	 * Registry authentication 
	 * 
	 * @param namespace
	 * @param repository
	 */
	private void registryAuthenticate(String namespace, String repository) {
        JsonObject resp = null;
		
		try {
			logger.info("Looking for any credentials for registry for " + this.registryUrl.getHost());

			String credKey = framework.getConfigurationPropertyService(DockerManagerImpl.NAMESPACE).getProperty(this.registryUrl.getHost(), "credentialsId");
			ICredentials creds = credService.getCredentials(credKey);

			if (creds instanceof ICredentialsUsernamePassword) {
				logger.info("Using username password authentication");
				ICredentialsUsernamePassword userPass = (ICredentialsUsernamePassword) creds;
				String user = userPass.getUsername();
				String password = userPass.getPassword();

				client.setAuthorisation(user, password).build();
				generateDockerServerAuthStructure(user, password);
				return;
			} 

			if (creds instanceof ICredentialsUsernameToken) {
				//TODO
			}

			if (creds instanceof ICredentialsUsername) {
				//TODO
			}

			if (creds instanceof ICredentialsToken) {
				//TODO
			}
		} catch (CredentialsException e) {
			logger.warn("Could not find credentials: " + e);
		} catch (ConfigurationPropertyStoreException e) {
			logger.warn("Could not access CPS: " + e);
		}
		logger.info("No credentials found, trying un-authenticated");
		
		// Try unauthorized
//		try {
//			resp = clientDockerAuth.getJson("/token?service=registry.docker.io&scope=repository:"+ namespace+"/"+repository+":pull").getContent();
//		} catch (HttpClientException e) {
//			logger.error("Could not post to registry", e);
//			return;
//		}
//
//		String token = "Bearer " + resp.get("token").getAsString();
//		client.addCommonHeader("Authorization", token);
    }
    
	/**
	 * Returns the host of the registry
	 * @return String
	 */
    public String getHost() {
		if (registryUrl.getPort() != -1) {
			return this.registryUrl.getHost() + ":" + this.registryUrl.getPort();
		} else {
			return this.registryUrl.getHost();
        }
	}   
	
	public String getAuthToken() {
		return this.authToken;
	}
}