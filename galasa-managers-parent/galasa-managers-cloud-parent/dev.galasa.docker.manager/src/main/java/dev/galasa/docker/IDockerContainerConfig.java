package dev.galasa.docker;

import java.util.HashMap;
import java.util.List;

/**
 * An interface for the annotation object that represents the configurations for a container.
 * 
 * @author James Davies
 */
public interface IDockerContainerConfig {

    /**
     * Allows for enivironment variables to be set on the container at startup
     * 
     * @param envs
     */
    public void setEnvs(HashMap<String,String> envs);

    /**
     * Returns the list of specified Keys and Values for the envs desired to be set
     * 
     * @return envs
     */
    public HashMap<String,String> getEnvs();

    /**
     * Lists all the provisioned/binded volumes for this configuration.
     * @return
     */
    public List<IDockerVolume> getVolumes();
    
}