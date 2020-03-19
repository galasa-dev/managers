/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.docker.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerManager;

public interface IDockerManagerSpi extends IDockerManager {
    
    /**
     * This method is able to provide the dockerContainer instance with a defined Dockerhub Container Tag
     */
    @NotNull
    IDockerContainer getDockerContainer(String dockerContainerTag) throws DockerManagerException;
  
}