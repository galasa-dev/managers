package dev.galasa.docker;

import java.util.HashMap;
import java.util.List;

public interface IDockerContainerConfig {

    public void setEnvs(HashMap<String,String> envs);

    public HashMap<String,String> getEnvs();

    public List<IDockerVolume> getVolumes();
    
}