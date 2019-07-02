package dev.voras.common.linux.internal;

import javax.validation.constraints.NotNull;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.ICommandShell;
import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.linux.LinuxManagerException;
import dev.voras.common.linux.internal.properties.SshCredentials;
import dev.voras.common.linux.internal.properties.SshPort;
import dev.voras.common.linux.spi.ILinuxProvisionedImage;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;

public class LinuxDSEImage implements ILinuxProvisionedImage {
	
	private final LinuxManagerImpl linuxManager;
	private final IConfigurationPropertyStoreService cps;
	private final String hostname4;
	private final String hostname6;
	private final String sshCredentialsId;
	private final int    sshPort;
	private final String tag;

	public LinuxDSEImage(LinuxManagerImpl manager, IConfigurationPropertyStoreService cps, String tag, String hostname4, String hostname6) throws LinuxManagerException, ConfigurationPropertyStoreException {
		this.linuxManager     = manager;
		this.cps              = cps;
		this.tag              = tag;
		this.hostname4        = hostname4;
		this.hostname6        = hostname6;
		this.sshPort          = SshPort.get(this.tag);
		this.sshCredentialsId = SshCredentials.get(this.tag);
	}

	@Override
	public @NotNull String getImageID() {
		return "dse" + tag;
	}

	@Override
	public @NotNull IIpHost getIpHost() {
		throw new UnsupportedOperationException("need to write");
	}

	@Override
	public @NotNull ICredentials getDefaultCredentials() throws LinuxManagerException {
		throw new UnsupportedOperationException("need to write");
	}

	@Override
	public @NotNull ICommandShell getCommandShell() throws LinuxManagerException {
		try {
			ICredentials credentials = linuxManager.getFramework().getCredentialsService().getCredentials(this.sshCredentialsId);
			
			if (credentials == null) {
				throw new LinuxManagerException("Unable to locate credentials " + this.sshCredentialsId + " for use with SSH command shell");
			}
			
			return this.linuxManager.getIpNetworkManager().getCommandShell(this.hostname4, this.sshPort, credentials); 
		} catch (LinuxManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new LinuxManagerException("Unable to initialise the command shell", e);
		}
	}

}
