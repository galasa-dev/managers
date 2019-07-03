package dev.voras.common.linux.internal;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.ICommandShell;
import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.ipnetwork.IpNetworkManagerException;
import dev.voras.common.linux.LinuxManagerException;
import dev.voras.common.linux.internal.properties.SshCredentials;
import dev.voras.common.linux.internal.properties.SshPort;
import dev.voras.common.linux.spi.ILinuxProvisionedImage;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.creds.CredentialsException;

public class LinuxDSEImage implements ILinuxProvisionedImage {
	
	private final Log logger = LogFactory.getLog(LinuxDSEImage.class);
	
	private final LinuxManagerImpl linuxManager;
	private final IConfigurationPropertyStoreService cps;
	private final String hostname4;
	private final String hostname6;
	private final String sshCredentialsId;
	private final int    sshPort;
	private final String tag;
	private final ICommandShell commandShell;
	private final FileSystem    fileSystem;
	private final LinuxDSEIpHost ipHost;
	
	private final Path          pathHome;
	private final Path          pathTemp;
	private final Path          pathRoot;

	public LinuxDSEImage(LinuxManagerImpl manager, IConfigurationPropertyStoreService cps, String tag, String hostname4, String hostname6) throws LinuxManagerException, ConfigurationPropertyStoreException {
		this.linuxManager     = manager;
		this.cps              = cps;
		this.tag              = tag;
		this.hostname4        = hostname4;
		this.hostname6        = hostname6;
		this.sshPort          = SshPort.get(this.tag);
		this.sshCredentialsId = SshCredentials.get(this.tag);
		this.commandShell     = createCommandShell();
		this.fileSystem       = createFileSystem();
		this.ipHost           = new LinuxDSEIpHost(this.hostname4);
		
		this.pathRoot         = this.fileSystem.getPath("/");
		this.pathTemp         = this.fileSystem.getPath("/tmp");
		
		try {
			String homeDir = this.commandShell.issueCommand("pwd");
			if (homeDir ==  null) {
				throw new LinuxManagerException("Unable to determine home directory, response null");
			}
			homeDir = homeDir.replaceAll("\\r\\n?|\\n", "");
			this.pathHome = this.fileSystem.getPath(homeDir);
			logger.info("Home directory for linux image tagged " + tag + " is " + homeDir);
		} catch (IpNetworkManagerException e) {
			throw new LinuxManagerException("Unable to determine home directory", e);
		}
		
	}

	private FileSystem createFileSystem() throws LinuxManagerException {
		try {
			ICredentials credentials = linuxManager.getFramework().getCredentialsService().getCredentials(this.sshCredentialsId);
			
			if (credentials == null) {
				throw new LinuxManagerException("Unable to locate credentials " + this.sshCredentialsId + " for use with SSH command shell");
			}
			
			return this.linuxManager.getIpNetworkManager().getFileSystem(this.hostname4, this.sshPort, credentials); 
		} catch (LinuxManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new LinuxManagerException("Unable to initialise the File System", e);
		}
	}

	private ICommandShell createCommandShell() throws LinuxManagerException {
		try {
			ICredentials credentials = getDefaultCredentials();
			
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
		try {
			return this.linuxManager.getFramework().getCredentialsService().getCredentials(this.sshCredentialsId);
		} catch (CredentialsException e) {
			throw new LinuxManagerException("Unable to obtain default credentials for linux host tagged " + this.tag, e);
		}
	}

	@Override
	public @NotNull ICommandShell getCommandShell() throws LinuxManagerException {
		try {
			ICredentials credentials = getDefaultCredentials();
			
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

	@Override
	public @NotNull Path getRoot() throws LinuxManagerException {
		return this.pathRoot;
	}

	@Override
	public @NotNull Path getHome() throws LinuxManagerException {
		return this.pathHome;
	}

	@Override
	public @NotNull Path getTmp() throws LinuxManagerException {
		return this.pathTemp;
	}

}
