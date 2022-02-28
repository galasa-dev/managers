/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.cloud.spi;

import java.util.Properties;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.cloud.ICloudContainer;

/**
 * SPI for the Cloud Manager
 * 
 * @author Michael Baylis
 *
 */
public interface ICloudManagerSpi {
	
	/**
	 * Retrieve the cloud container by tag
	 * 
	 * @param tag - The tag name
	 * @return the Cloud Container if found
	 * @throws CloudManagerException - If the tag is unknown
	 */
	public ICloudContainer getCloudContainerByTag(String tag) throws CloudManagerException;
	
	/**
	 * Generate a new Cloud Container, normal called by an application Manager during provision generate,
	 * but can be called at anytime (exception shutdown)
	 * 
	 * @param tag - The tag to assign, must not exist already
	 * @param provider - optional (null), the provider to use to provision the Cloud Container
	 * @param image - The image name to use
	 * @param ports - The ports to be exposed
	 * @param environmentProperties - Any environment properties to provide to the container
	 * @param runArguements - Any run arguements to start the container with
	 * @param autoStart - The container is to be started during provisionStart, only relevant if this method is called during provisionGenerate.
	 * @param startOrder - If autoStart = true, then the order the container is to be started, only relevant if this method is called during provisionGenerate.
	 * @return a ICloudContainer instance.
	 * @throws CloudManagerException
	 */
	public ICloudContainer generateCloudContainer(
			String                tag,
			String                provider,
			String                image,
			CloudContainerPort    ports[],
			Properties            environmentProperties,
			String                runArguements[],
			boolean               autoStart,
			int                   startOrder) throws CloudManagerException;

	/**
	 * Register a provider for Cloud Containers
	 * 
	 * @param containerProvider A Cloud Container provisioner
	 */
	public void registerCloudContainerProvider(ICloudContainerProvider containerProvider);
}
