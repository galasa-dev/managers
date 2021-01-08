package dev.galasa.docker;

public interface IDockerVolume {

    public String getVoumeName();

    public String getMountPath();

    public boolean readOnly();
    
}