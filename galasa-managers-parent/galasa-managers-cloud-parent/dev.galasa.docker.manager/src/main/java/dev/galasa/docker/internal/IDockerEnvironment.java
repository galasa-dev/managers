package dev.galasa.docker.internal;

import java.util.Collection;
import java.util.List;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;

public interface IDockerEnvironment {
	/**
	 * Generate the docker containers on specified docker engines.
	 * 
	 * @param testClasses
	 * @throws DockerProvisionException
	 */
	public void generate(List<Class<?>> testClasses) throws DockerProvisionException;
	
	/**
	 * Build all docker resources, including finding and pulling images, and building containers.
	 * 
	 * @param testClasses
	 * @throws DockerProvisionException
	 */
	public void build(List<Class<?>> testClasses) throws DockerProvisionException;

	/**
	 * Clean up the docker environment, discard all containers in the instance.
	 * 
	 * @throws DockerManagerException
	 */
	public void discard() throws DockerManagerException;

	/**
	 * Return the docker engine implementation used in this docker environment instance.
	 * 
	 * @return
	 * @throws DockerManagerException
	 */
	public DockerEngineImpl getDockerEngineImpl(String dockerEngineTag) throws DockerManagerException;
	
	/**
	 * Return a specified docker containers implementation
	 * 
	 * @param dockerContainerTag
	 * @return
	 * @throws DockerManagerException
	 */
	public DockerContainerImpl getDockerContainerImpl(String dockerContainerTag) throws DockerManagerException;

	/**
	 * Return a collection of all the docker containers running in this environment, via there tags
	 * 
	 * @return
	 */
	public Collection<DockerContainerImpl> getContainers();

	/**
	 * Provision a docker container of the passed specifications.
	 * 
	 * @param tag
	 * @param image
	 * @param start
	 * @return
	 * @throws DockerProvisionException
	 */
	public DockerContainerImpl provisionDockerContainer(String tag, String image, boolean start, String DockerEngineTag) throws DockerProvisionException;
	//public void preAllocate(Resource rm) throws ResourceManagementException;

	/**
	 *  Free up the docker slot used to house a docker container.
	 * 
	 * @param dockerSlot
	 * @throws DockerProvisionException
	 */
	public void freeDockerSlot(DockerSlotImpl dockerSlot) throws DockerProvisionException;
	//public void abortPreAllocation();
}