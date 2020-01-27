/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.docker;  

public interface IDockerManager {

    IDockerContainer getDockerContainer(String dockerContainerTag) throws DockerManagerException;
}