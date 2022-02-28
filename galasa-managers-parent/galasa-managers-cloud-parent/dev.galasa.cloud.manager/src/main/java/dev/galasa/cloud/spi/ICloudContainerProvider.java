/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.cloud.spi;

import java.util.Properties;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.cloud.ICloudContainer;

/**
 * Interface from the Cloud Manager to Cloud Container Providers 
 * 
 * @author Michael Baylis
 *
 */
public interface ICloudContainerProvider {
	
	/**
	 * Generate a new Cloud Container
	 * 
	 * @param tag - The tag to assign
	 * @param image - The image name to use, at this stage this will be the full image name
	 * @param ports - The ports to expose
	 * @param environmentProperties - Environment properties to provide to the container
	 * @param runArguements - Run arguements to provide to the container
	 * @return A ICloudContainer instance
	 * @throws CloudManagerException
	 */
	public ICloudContainer generateCloudContainer(
			String                tag,
			String                image,
			ICloudContainerPort   ports[],
			Properties            environmentProperties,
			String                runArguements[]) throws CloudManagerException;

}
