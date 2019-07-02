package dev.voras.common.linux;

import javax.validation.constraints.NotNull;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.ICommandShell;
import dev.voras.common.ipnetwork.IIpHost;

/**
 * <p>Represents a Linux Image .</p>
 * 
 * <p>Use a {@link LinuxImage} annotation to populate this field with</p>
 * 
 * @author Michael Baylis
 *
 */
public interface ILinuxImage {

	/**
	 * Get the name of the Linux Image
	 * 
	 * @return The image ID, never null
	 */
	@NotNull
	String getImageID();
	
	/**
	 * Retrieve the IP Network Host details
	 * 
	 * @return
	 */
	@NotNull
	IIpHost getIpHost();
	
	/**
	 * Retrieve the default credentials for the zOS Image. 
	 * 
	 * @return The default credentials - see {@link dev.voras.framework.spi.creds.ICredentials}
	 * @throws LinuxManagerException if the credentials are missing or there is a problem with the credentials store
	 */
	@NotNull
	ICredentials getDefaultCredentials() throws LinuxManagerException;
	
	@NotNull
	ICommandShell getCommandShell() throws LinuxManagerException;
}
