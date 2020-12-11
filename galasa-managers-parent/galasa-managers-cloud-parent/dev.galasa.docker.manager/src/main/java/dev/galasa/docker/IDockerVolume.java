package dev.galasa.docker;

import com.google.gson.JsonObject;

/**
 * Docker Volume Resource Object
 * <p>
 * Provides a object representing a docker volume that is found on a docker engine. Upon 
 * instantiation the object will either bind to an exsisting volume on the engine, or 
 * create a new one. A container requets the use of volume in the annotation within the test.
 * <code>@DockerContainer(dockerContainerTag="a", image="galasadocker/ivp:1.0.0", volumes = "volume1:/tmp")<br>
 * The path to mount the volume is also provided with the request. "volumes" is a comma
 * seperated list, incase multiple volumes are required.
 * <p>
 * 
 * @author James Davies
 *
 */
public interface IDockerVolume {

    /**
     * To be called from a containerCreate method. Provides the json object required for 
     * mounting a volume to the container.
     * 
     * @return JsonObject
     * @throws DockerManagerException 
     */
    public JsonObject getMountJson() throws DockerManagerException;
    
}