package dev.galasa.docker.internal;

import java.util.List;

import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerImage;
import dev.galasa.framework.spi.IFramework;

/**
 * DockerImageImpl. Allows for the checking of images on registries and pulling of images to the docker server.
 * 
 * @author James Davies
 */
public class DockerImageImpl implements IDockerImage {
    private final IFramework            framework;
    private final DockerManagerImpl     dockerManager;
    private final DockerServerImpl      dockerServer;
    private final String                imageName;
    private String                      fullName;
    private String                      authToken;
    
    private boolean                     authRequired = false;

    private static final Log            logger = LogFactory.getLog(DockerImageImpl.class);

    /**
     * DockerImageImpl
     * 
     * @param framework
     * @param dockerManager
     * @param dockerServer
     * @param imageName
     */
    public DockerImageImpl(IFramework framework, DockerManagerImpl dockerManager, 
            DockerServerImpl dockerServer, String imageName) {
        this.framework      = framework;
        this.dockerManager  = dockerManager;
        this.dockerServer   = dockerServer;
        this.imageName      = imageName;
    }

    /**
     * returns the full name of the docker container
     * 
     * @return String
     */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Using the full name to try and locate the image in question in the registries or locally on the docker server. 
     * 
     * @throws DockerManagerException
     */
    public void locateImage() throws DockerManagerException {
        if (this.fullName != null) {
            logger.info("Image already located.");
            return;
        }

        String namespace  = "";
		String repository = "";
        String tag        = "";
        
        String workingName = this.imageName;
		int pos = workingName.indexOf("/");
		if (pos >= 0) {
			namespace = workingName.substring(0, pos);
			workingName = workingName.substring(pos + 1);
		}
		
		pos = workingName.indexOf(":");
		if (pos >= 0) {
			tag = workingName.substring(pos + 1);
			workingName = workingName.substring(0, pos);
		}
		if (tag.isEmpty()) {
			tag = "latest";
		}
		
		repository = workingName; 
        if (namespace.isEmpty()) {
            namespace = "library";
        }
        workingName = namespace + "/" + repository + ":" + tag;
		
		List<DockerRegistryImpl> registries = dockerManager.getRegistries();
		for(DockerRegistryImpl registry : registries) {
			if (registry.doYouHave(namespace, repository, tag)) {
                this.fullName = registry.getHost() + "/" + workingName;
                this.authToken = registry.getAuthToken();
                if (this.authToken != null) {
                    authRequired = true;
                }
				logger.info("Docker Image located in registry: " + registry.getHost());
				logger.info( "Docker image full name is '" + this.fullName + "'");
				return;
            }
        }

        JsonObject image = dockerServer.getImage(workingName);
		if (image != null) {
			this.fullName = workingName;
			logger.info("Docker Image located only on the server as name '" + this.fullName + "'");
			return;
		}
		
		throw new DockerManagerException("Unable to locate Docker Image '" + workingName + "'");
    }

    /**
     * Sets the full name of the image
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Pulls the image onto the docker server.
     * 
     * @throws DockerManagerException
     */
    public void pullImage() throws DockerManagerException {
        String pull;
        if (this.fullName == null) {
            throw new DockerManagerException("Unable to pull image, full image name not set");
        }

        if(authRequired) {
            pull = dockerServer.pullImage(this.fullName, this.authToken);
        } else {
            pull = dockerServer.pullImage(this.fullName);
        }
        if (pull == null) {
            throw new DockerManagerException("Docker daemon did not respond to pull request");
        }

        if (pull.contains("Status: Downloaded newer image")) {
			logger.info("Docker Image " + this.fullName + " pulled");
		} else if (pull.contains("Status: Image is up to date")) {
			logger.info("Docker Image " + this.fullName + " was not pulled, current image up to date");
		} else {
			throw new DockerManagerException("Unrecognised response from pull request\n" + pull);
		}
    }
}