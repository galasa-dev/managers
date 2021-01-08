package dev.galasa.docker;

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
    
}