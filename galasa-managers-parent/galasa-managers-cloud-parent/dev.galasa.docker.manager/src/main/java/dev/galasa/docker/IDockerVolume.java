/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.docker;

import java.io.InputStream;

/** 
 * A Galasa object to track, bind and provision Docker volumes with.
 * 
 * @author James Davies
*/
public interface IDockerVolume {

    /**
     * Returns the volume names, specified or provisioned.
     * 
     * @return String volumeName
     */
    public String getVolumeName();

      /**
     * Return the volume tag
     * 
     * @return String volumeName
     */
    public String getVolumeTag();

    /**
     * Returns the specified mount path.
     * @return String mountPath
     */
    public String getMountPath();

    /**
     * Returns the read state of the volume.
     * 
     * @return boolean readOnly
     */
    public boolean readOnly();

    /**
     * Get the Tag of the engine used to host the volume.
     * 
     * @return
     */
    public String getEngineTag();


    /**
     * Pre-populate a volume with some data. The filename needs to be passed
     * 
     * @param fileName
     * @param data
     */
    public void LoadFile(String fileName, InputStream data) throws DockerManagerException;

    /**
     * Pre-populate a volume with some string data. Filename is required.
     * 
     * @param fileName
     * @param data
     */
    public void LoadFileAsString(String fileName, String data) throws DockerManagerException;
    
    
    /**
     * Will execute a `chown USER:GROUP filename` on a file inside the volume.
     * 
     * @param userGroup
     * @param filename
     * @throws DockerManagerException
     */
    public void fileChown(String userGroup, String filename) throws DockerManagerException;
    
    /**
     * Will execute a `chmod XXX filename` where the XXX is the permission string, on a file inside a volume.
     * 
     * @param permissions
     * @param filename
     * @throws DockerManagerException
     */
    public void fileChmod(String permissions, String filename) throws DockerManagerException;
    
}