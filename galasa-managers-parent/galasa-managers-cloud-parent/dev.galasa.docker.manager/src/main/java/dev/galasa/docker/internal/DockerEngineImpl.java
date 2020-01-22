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
import dev.galasa.docker.IDockerEngine;
import dev.galasa.docker.internal.properties.DockerEngine;
import dev.galasa.docker.internal.properties.DockerEnginePort;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;

public class DockerEngineImpl implements IDockerEngine {
    private IFramework              framework;
    private DockerManagerImpl       dockerManager;
    private final IHttpClient       dockerEngineClient;
    private final URI 				uri;

	private String 					dockerEngineId;
    private String                  dockerVersion;
    private String                  apiVersion;  

    private static final Log        logger = LogFactory.getLog(DockerEngine.class);
    

	/**
	 * Docker Engine Implementation. This provides all the docker engine API calls to perform
	 * docker commands on a specified engine running docker.
	 * 
	 * @param framework
	 * @param dockerManager
	 * @throws DockerProvisionException
	 */
    public DockerEngineImpl(IFramework framework, DockerManagerImpl dockerManager, String dockerEngineTag) throws DockerProvisionException {
        this.framework          = framework;
		this.dockerManager      = dockerManager;
		this.dockerEngineId		= dockerEngineTag;
        
        dockerEngineClient = dockerManager.httpManager.newHttpClient();
        try {
			String engine = DockerEngine.get(this);
			String port = DockerEnginePort.get(this);

			if (engine != null && port != null) {
				this.uri = new URI(engine + ":" + port);
				IHttpClient httpClient2 = dockerEngineClient;
                httpClient2.setURI(this.uri);
			} else {
				throw new DockerProvisionException("Could not retrieve proper endpoint for docker engine: Engine - " + engine + ", Port - " + port);
			}

			logger.info("Docker Engine is set to " + engine.toString());
		} catch (Exception e) {
			throw new DockerProvisionException("Unable to instantiate Docker Engine", e);
		}

	}
	
	public String getEngineId(){
		return this.dockerEngineId;
	}

	/**
	 * Checks the docker engine is contactable.
	 * @throws DockerProvisionException
	 */
	public void checkEngine() throws DockerProvisionException {
        try {
            JsonObject jsonVersion = getJson("/version");

			if(jsonVersion != null) {
            	dockerVersion = jsonVersion.get("Version").getAsString();
				apiVersion = jsonVersion.get("ApiVersion").getAsString();
			}

            logger.info("Docker engine is running, version: "+ dockerVersion + ", apiVersion: " + apiVersion);
        } catch (DockerManagerException e) {
            throw new DockerProvisionException("Unable to validate docker engine connectivity.", e);
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
			HttpClientResponse<String> response = dockerEngineClient.getText(path);
			
			String repsString = response.getContent();

			switch(response.getStatusCode()) {
				case HttpStatus.SC_OK: 
					return repsString;
				case HttpStatus.SC_NOT_FOUND:
					return null;
			}

			logger.error("Get Log failed to docker engine - " + response.getStatusLine().toString() + "\n" + repsString);
			throw new DockerManagerException("Log Get failed to docker engine - " + response.getStatusLine().toString());
		} catch (Exception e) {
			throw new DockerManagerException("Get Log failed to docker engine", e);
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

		dockerEngineClient.addCommonHeader("X-Registry-Auth", registryToken);
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
	 * Deletes a container from the docker engine using container id.
	 * 
	 * @param containerId
	 * @return String response
	 * @throws DockerManagerException
	 */
    public String deleteContainer(@NotNull String containerId) throws DockerManagerException {
        return deleteString("/containers/" + containerId + "?force=true&v=true");
    }

	/**
	 * Starts a docker container on the docker engine from the container id.
	 * 
	 * @param containerId
	 * @return
	 * @throws DockerManagerException
	 */
    public String startContainer(@NotNull String containerId) throws DockerManagerException {
        return postString("/containers/" + containerId + "/start", "");
    }

	/**
	 * Sends commands through to a docker container running on the docker engine using the container id.
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
	 * Returns docker engine host
	 * @return String
	 */
    public String getHost() {
        return this.uri.getHost();
    }

	/**
	 * returns the docker engine URI
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
		HttpClientResponse<String> response = dockerEngineClient.deleteText(path);
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

			logger.error("Delete failed to docker engine - " + resp);
			throw new DockerManagerException("Delete failed to docker engine - " +resp);
		} catch (Exception e) {
			dockerEngineClient.close();
			throw new DockerManagerException("Delete failed to docker engine", e);
		}
	}

	/**
	 * Performs a HTTP GET to the docker engine to a specified path
	 * 
	 * @param path
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
    private JsonObject getJson(String path) throws DockerManagerException {
        try {
            HttpClientResponse<JsonObject> response = dockerEngineClient.getJson(path);

            JsonObject jsonResponse = response.getContent();

            switch(response.getStatusCode()) {
                case HttpStatus.SC_OK: 
                    return jsonResponse;
                case HttpStatus.SC_NOT_FOUND:
                    return null;
                }
                logger.error("Get failed to docker engine - " + response.getStatusLine().toString() + "\n" + jsonResponse.getAsString());
                throw new DockerManagerException("Get failed to docker engine - " + response.getStatusLine().toString());
            } catch(Exception e) {
                throw new DockerManagerException("Failed to get from Docker engine: ", e);
            }
    }

	/**
	 * Performs a HTTP POST to the docker engine to a specified path with a json body.
	 * 
	 * @param path
	 * @param data
	 * @return JsonObject
	 * @throws DockerManagerException
	 */
    private JsonObject postJson(String path, JsonObject data) throws DockerManagerException {
		try {
			HttpClientResponse<JsonObject> json = dockerEngineClient.postJson(path, data);
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
	
				logger.error("Post failed to docker engine - " + response.getAsString());
				throw new DockerManagerException("Post failed to docker engine - " + response.getAsString());
			} catch (Exception e) {
				throw new DockerManagerException("Post failed to docker engine", e);
			}
	}

	/**
	 * Performs a HTTP POST to the docker engine to a specified path with a text body.
	 * @param path
	 * @param data
	 * @return
	 * @throws DockerManagerException
	 */
	private String postString(String path, String data) throws DockerManagerException {
		try {
			logger.debug("Posting: " + data + "to the endpoint: " + path);
			HttpClientResponse<String> response = dockerEngineClient.postText(path, data);
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

			logger.error("Post failed to docker engine - " + resp);
			throw new DockerManagerException("Post failed to docker engine - " +resp);
		} catch (Exception e) {
			dockerEngineClient.close();
			throw new DockerManagerException("Post failed to docker engine", e);
		}
	}
	
	//TODO: Need to work out how to authenticate the docker engine to the registry
}