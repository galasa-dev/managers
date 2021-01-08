package dev.galasa.docker.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.galasa.docker.IDockerContainerConfig;
import dev.galasa.docker.IDockerVolume;

public class DockerContainerConfigImpl implements IDockerContainerConfig {
    private List<IDockerVolume> volumes = new ArrayList<>();
    private HashMap<String,String> envs = new HashMap<>();

    public DockerContainerConfigImpl(List<IDockerVolume> volumes) {
        this.volumes = volumes;
    }

    @Override
    public void setEnvs(HashMap<String, String> envs) {
       this.envs = envs;
    }

    @Override
    public HashMap<String,String> getEnvs() {
        return this.envs;
    }

    @Override
    public List<IDockerVolume> getVolumes() {
        return this.volumes;
    }
    
}