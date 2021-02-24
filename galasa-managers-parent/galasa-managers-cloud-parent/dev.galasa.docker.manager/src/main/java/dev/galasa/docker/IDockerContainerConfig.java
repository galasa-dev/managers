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

    /**
     * Return a volume from the configuration of a specfic tag. 
     * 
     * @return
     */
    public IDockerVolume getVolumeByTag(String volumeTag);
    

    /**
     * Allows a set of ports to be exposed inside the container before startup. Ports are to be expressed for the
     * conatiner and not the host (I.e. "8080/tcp"), as a random port will be exposed on the host, which can be
     * obtained on the container object
     * 
     * @param ports
     */
    public void setExposedPorts(List<String> ports);

    /**
     * Retruns a list of exposed ports
     * 
     * @return
     */
    public List<String> getExposedPorts();
}