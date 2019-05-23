package io.ejat.zos;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.creds.ICredentials;

/**
 * <p>Represents a zOS Image (or lpar).</p>
 * 
 * <p>Use a {@link ZosImage} annotation to populate this field with</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IZosImage {

	/**
	 * Get the name of the zOS Image, may be different from the SMFID
	 * 
	 * @return The image ID, never null
	 */
	@NotNull
	String getImageID();
	
	/**
	 * Get the name of the Sysplex this Image belongs to
	 * 
	 * @return the sysplex id, can be null if the image is not in a sysplex (defined by the presence of an attached Coupling Facility)
	 */
	String getSysplexID();
	
	/**
	 * Get the name of the Cluster this Image belongs to
	 * 
	 *  @return a non-null String representing the cluster the image was allocated from 
	 */
	@NotNull
	String getClusterID();

	/**
	 * Retrieve the default credentials for the zOS Image. 
	 * 
	 * @return The default credentials - see {@link io.ejat.framework.spi.creds.ICredentials}
	 * @throws ZosManagerException if the credentials are missing or there is a problem with the credentials store
	 */
	@NotNull
	ICredentials getDefaultCredentials() throws ZosManagerException;
}
