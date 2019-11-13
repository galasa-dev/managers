package dev.galasa.docker;  

public interface IDockerManager {

    IDockerContainer getDockerContainer(String dockerContainerTag) throws DockerManagerException;
}