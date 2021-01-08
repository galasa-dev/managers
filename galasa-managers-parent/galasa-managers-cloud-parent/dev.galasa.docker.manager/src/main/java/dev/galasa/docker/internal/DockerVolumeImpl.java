package dev.galasa.docker.internal;

import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerVolume;

/**
 * A implementation of the docker volumes that will be brought up on the engine
 * 
 * @author James Davies
 */
public class DockerVolumeImpl implements IDockerVolume {
    private final String volumeName;
    private final String mountPath;
    private final DockerEngineImpl engine;
    private final boolean readOnly;

    private static final Log logger = LogFactory.getLog(DockerVolumeImpl.class);

    /**
     * Constructor that determines the nature of the volume (readOnly or not), and provisions or ensures
     * the volumes exsists.
     * 
     * @param volumeName
     * @param mountPath
     * @param engine
     * @throws DockerManagerException
     */
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

    /**
     * Returns volume name
     * 
     * @return volumeName
     */
    @Override
    public String getVolumeName() {
        return this.volumeName;
    }

     /**
     * Returns mount path
     * 
     * @return mountPath
     */
    @Override
    public String getMountPath() {
        return this.mountPath;
    }

     /**
     * Returns readonly state
     * 
     * @return readOnly
     */
    @Override
    public boolean readOnly() {
        return this.readOnly;
    }

     /**
     * Checks the docker engine for a specific named volume
     * 
     * @return boolean exists
     */
    public boolean doesVolumeExist() throws DockerManagerException {
        if (engine.getVolume(this.volumeName) != null) {
            return true;   
        }
        return false;
    }

    /**
     * Discard a volume. This should only be called for galasa created volumes.
     * 
     * @throws DockerManagerException
     */
    public void discard() throws DockerManagerException {
        if (readOnly) {
            logger.error("Not deleted, not a Galasa volume!");
        } else {
            engine.deleteVolume(this.volumeName);
        }
    }
    
}