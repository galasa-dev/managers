package dev.galasa.docker.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.IDockerServer;
import dev.galasa.docker.internal.properties.DockerServer;
import dev.galasa.docker.internal.properties.DockerServerPort;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;

public class DockerServerImpl implements IDockerServer {
    private IFramework              framework;
    private DockerManagerImpl       dockerManager;
    private final IHttpClient       dockerSeverClient;
    private final URI 				uri;

    private String                  dockerVersion;
    private String                  apiVersion;  

    private static final Log        logger = LogFactory.getLog(DockerServer.class);
    

	/**
	 * Docker Server Implementation. This provides all the docker engine API calls to perform
	 * docker commands on a specified server running docker.
	 * 
	 * @param framework
	 * @param dockerManager
	 * @throws DockerProvisionException
	 */
    public DockerServerImpl(IFramework framework, DockerManagerImpl dockerManager) throws DockerProvisionException {
        this.framework          = framework;
        this.dockerManager      = dockerManager;
        
        dockerSeverClient = dockerManager.httpManager.newHttpClient();
        try {
			String server = DockerServer.get();
			String port = DockerServerPort.get();

			if (server != null && port != null) {
				this.uri = new URI("http://" + server + ":" + port);
				IHttpClient httpClient2 = dockerSeverClient;
                httpClient2.setURI(this.uri);
			} else {
				throw new DockerProvisionException("Could not retrieve proper endpoint for docker server: Server - " + server + ", Port - " + port);
			}

			logger.info("Docker Server is set to " + server.toString());
		} catch (Exception e) {
			throw new DockerProvisionException("Unable to instantiate Docker Server", e);
		}

    }

	/**
	 * Checks the docker server is contactable.
	 * @throws DockerProvisionException
	 */
	public void checkServer() throws DockerProvisionException {
        try {
            JsonObject jsonVersion = getJson("/version");

			if(jsonVersion != null) {
            	dockerVersion = jsonVersion.get("Version").getAsString();
				apiVersion = jsonVersion.get("ApiVersion").getAsString();
			}

            logger.info("Docker server is running, version: "+ dockerVersion + ", apiVersion: " + apiVersion);
        } catch (DockerManagerException e) {
            throw new DockerProvisionException("Unable to validate docker server connectivity.", e);
        }
    }

    /**
	 * Retrieve docker logs from a docker container from the url
	 * 
	 * @param path
	 * @return
	 * @throws DockerManagerException
	 */
    public String getLog(String path) throws DockerManagerException {
		try {
			HttpClientResponse<String> response = dockerSeverClient.getText(path);
			
			String repsString = response.getContent();

			switch(response.getStatusCode()) {
				case HttpStatus.SC_OK: 
					return repsString;
				case HttpStatus.SC_NOT_FOUND:
					return null;
			}

			logger.error("Get Log failed to docker server - " + response.getStatusLine().toString() + "\n" + repsString);
			throw new DockerManagerException("Log Get failed to docker server - " + response.getStatusLine().toString());
		} catch (Exception e) {
			throw new DockerManagerException("Get Log failed to docker server", e);
		}
    }
    /**
	 * Pull docker image from registries
	 * 
	 * @param fullName
	 * @return String repsonse
	 * @throws DockerManagerException
	 */
    public String pullImage(@NotNull String fullName) throws DockerManagerException {
		return postString("/images/create?fromImage=" + fullName, "");
	}
	
	public String pullImage(@NotNull String fullName, String registryToken) throws DockerManagerException {

		dockerSeverClient.addCommonHeader("X-Registry-Auth", registryToken);
		return pullImage(fullName);
	}

	/**
	 * Retrieves the image information
	 * @param imageName
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
    public JsonObject getImage(@NotNull String imageName) throws DockerManagerException {
		return getJson("/images/" + imageName + "/json");
    }
	
	/**
	 * Retrieves container information from the container id
	 * 
	 * @param containerId
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
    public JsonObject getContainer(@NotNull String containerId) throws DockerManagerException {
		return getJson("/containers/" + containerId + "/json");
    }
	
	/**
	 * Creates a container from a image name
	 * 
	 * @param containerName
	 * @param imageData
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
    public JsonObject createContainer(@NotNull String imageName, JsonObject imageData) throws DockerManagerException {
        return postJson("/containers/create?name=" + imageName, imageData);
	}

	/**
	 * Kills a container from it's container id
	 * 
	 * @param containerId
	 * @return String response
	 * @throws DockerManagerException
	 */
    public String killContainer(@NotNull String containerId) throws DockerManagerException {
        return postString("/containers/" + containerId + "/kill", "");
    }

	/**
	 * Deletes a container from the docker server using container id.
	 * 
	 * @param containerId
	 * @return String response
	 * @throws DockerManagerException
	 */
    public String deleteContainer(@NotNull String containerId) throws DockerManagerException {
        return deleteString("/containers/" + containerId + "?force=true&v=true");
    }

	/**
	 * Starts a docker container on the docker server from the container id.
	 * 
	 * @param containerId
	 * @return
	 * @throws DockerManagerException
	 */
    public String startContainer(@NotNull String containerId) throws DockerManagerException {
        return postString("/containers/" + containerId + "/start", "");
    }

	/**
	 * Sends commands through to a docker container running on the docker server using the container id.
	 * 
	 * @param containerId
	 * @param commandData
	 * @return
	 * @throws DockerManagerException
	 */
    public JsonObject sendExecCommands(@NotNull String containerId, JsonObject commandData)
            throws DockerManagerException {
        return postJson("/containers/" + containerId + "/exec", commandData);
    }

	/**
	 * Returns the state of exec commands being performed on a docker container using container id.
	 * 
	 * @param containerId
	 * @return
	 * @throws DockerManagerException
	 */
    public JsonObject getExecInfo(@NotNull String containerId) throws DockerManagerException {
        return getJson("/exec/"+ containerId + "/json");
    }
	
	/**
	 * Returns docker server host
	 * @return String
	 */
    public String getHost() {
        return this.uri.getHost();
    }

	/**
	 * returns the docker server URI
	 * @return URI
	 * @throws URISyntaxException
	 */
    public URI getURI() throws URISyntaxException {
		return this.uri;
	}

	/**
	 * Issues a HTTP DELETE command to the specified path
	 * 
	 * @param path
	 * @return String response
	 * @throws DockerManagerException
	 */
	private String deleteString(String path) throws DockerManagerException{
		try{
		HttpClientResponse<String> response = dockerSeverClient.deleteText(path);
		String resp = response.getContent();
			
			switch(response.getStatusCode()) {
			case HttpStatus.SC_OK: 
			case HttpStatus.SC_CREATED: 
				if (resp == null) {
					return null;
				}
				return resp;
			case HttpStatus.SC_NO_CONTENT:
			case HttpStatus.SC_NOT_FOUND:
				return null;
			}

			logger.error("Delete failed to docker server - " + resp);
			throw new DockerManagerException("Delete failed to docker server - " +resp);
		} catch (Exception e) {
			dockerSeverClient.close();
			throw new DockerManagerException("Delete failed to docker server", e);
		}
	}

	/**
	 * Performs a HTTP GET to the docker server to a specified path
	 * 
	 * @param path
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
    private JsonObject getJson(String path) throws DockerManagerException {
        try {
            HttpClientResponse<JsonObject> response = dockerSeverClient.getJson(path);

            JsonObject jsonResponse = response.getContent();

            switch(response.getStatusCode()) {
                case HttpStatus.SC_OK: 
                    return jsonResponse;
                case HttpStatus.SC_NOT_FOUND:
                    return null;
                }
                logger.error("Get failed to docker server - " + response.getStatusLine().toString() + "\n" + jsonResponse.getAsString());
                throw new DockerManagerException("Get failed to docker server - " + response.getStatusLine().toString());
            } catch(Exception e) {
                throw new DockerManagerException("Failed to get from Docker server: ", e);
            }
    }

	/**
	 * Performs a HTTP POST to the docker server to a specified path with a json body.
	 * 
	 * @param path
	 * @param data
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
    private JsonObject postJson(String path, JsonObject data) throws DockerManagerException {
		try {
			HttpClientResponse<JsonObject> json = dockerSeverClient.postJson(path, data);
			JsonObject response = json.getContent();

			switch(json.getStatusCode()) {
				case HttpStatus.SC_OK: 
				case HttpStatus.SC_CREATED: 
					if (response == null) {
						return null;
					}
					return response;
				case HttpStatus.SC_NO_CONTENT:
				case HttpStatus.SC_NOT_FOUND:
					return null;
				}
	
				logger.error("Post failed to docker server - " + response.getAsString());
				throw new DockerManagerException("Post failed to docker server - " + response.getAsString());
			} catch (Exception e) {
				throw new DockerManagerException("Post failed to docker server", e);
			}
	}

	/**
	 * Performs a HTTP POST to the docker server to a specified path with a text body.
	 * @param path
	 * @param data
	 * @return
	 * @throws DockerManagerException
	 */
	private String postString(String path, String data) throws DockerManagerException {
		try {
			logger.debug("Posting: " + data + "to the endpoint: " + path);
			HttpClientResponse<String> response = dockerSeverClient.postText(path, data);
			String resp = response.getContent();
			
			switch(response.getStatusCode()) {
			case HttpStatus.SC_OK: 
			case HttpStatus.SC_CREATED: 
				if (resp == null) {
					return null;
				}
				return resp;
			case HttpStatus.SC_NO_CONTENT:
			case HttpStatus.SC_NOT_FOUND:
				return null;
			}

			logger.error("Post failed to docker server - " + resp);
			throw new DockerManagerException("Post failed to docker server - " +resp);
		} catch (Exception e) {
			dockerSeverClient.close();
			throw new DockerManagerException("Post failed to docker server", e);
		}
	}
	
	//TODO: Need to work out how to authenticate the docker server to the registry
}