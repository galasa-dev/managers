package dev.galasa.docker.internal;

import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerVolume;
import dev.galasa.framework.spi.IFramework;

public class DockerVolumeImpl implements IDockerVolume {
    private final String volumeName;
    private final String mountPath;
    private final DockerEngineImpl engine;
    private final boolean readOnly;

    private static final Log logger = LogFactory.getLog(DockerVolumeImpl.class);

    public DockerVolumeImpl(String volumeName, String mountPath, DockerEngineImpl engine)
            throws DockerManagerException {

        // Non Galasa made volumes should not be edited by Galasa run containers.
        if ("".equals(volumeName)) {
            this.readOnly = false;
            logger.info("Generating volume");

            // If no named passed, Docker will name the volume. We will add galasa labels.
            JsonObject json = engine.createVolume(volumeName);
            this.volumeName = json.get("Name").getAsString();
        } else {
            this.volumeName = volumeName;
            this.readOnly = true;
        }
   
        this.mountPath = mountPath;
        this.engine = engine;

        if(!doesVolumeExist()) {
            logger.error("No volume found with name: " + this.volumeName);
            throw new DockerManagerException("Could not find volume with name: " + this.volumeName);
            
        }
        logger.info("Existing volume found.");
    }

    @Override
    public String getVoumeName() {
        return this.volumeName;
    }

    @Override
    public String getMountPath() {
        return this.mountPath;
    }

    @Override
    public boolean readOnly() {
        return this.readOnly;
    }

    public boolean doesVolumeExist() throws DockerManagerException {
        if (engine.getVolume(this.volumeName) != null) {
            return true;   
        }
        return false;
    }

    public void discard() throws DockerManagerException {
        engine.deleteVolume(this.volumeName);
    }
    
}