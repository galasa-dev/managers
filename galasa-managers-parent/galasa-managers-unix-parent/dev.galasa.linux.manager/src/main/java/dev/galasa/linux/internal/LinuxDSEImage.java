package dev.galasa.linux.internal;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.spi.ILinuxProvisionedImage;

public class LinuxDSEImage implements ILinuxProvisionedImage {

	private final Log logger = LogFactory.getLog(LinuxDSEImage.class);

	private final LinuxManagerImpl linuxManager;
	private final IConfigurationPropertyStoreService cps;
	private final String tag;
	private final ICommandShell commandShell;
	private final FileSystem    fileSystem;
	private final LinuxDSEIpHost ipHost;
	private final String         hostid;

	private final Path          pathHome;
	private final Path          pathTemp;
	private final Path          pathRoot;

	public LinuxDSEImage(LinuxManagerImpl manager, IConfigurationPropertyStoreService cps, String tag, String hostid) throws LinuxManagerException, ConfigurationPropertyStoreException {
		this.linuxManager     = manager;
		this.cps              = cps;
		this.tag              = tag;
		this.hostid           = hostid;
		this.commandShell     = createCommandShell();
		this.fileSystem       = createFileSystem();

		try {
			this.ipHost           = new LinuxDSEIpHost(this.linuxManager, hostid);
		} catch(Exception e) {
			throw new LinuxManagerException("Unable to create the IP Host for host " + this.hostid, e);
		}


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
			return this.linuxManager.getIpNetworkManager().getFileSystem(this.ipHost); 
		} catch (Exception e) {
			throw new LinuxManagerException("Unable to initialise the File System", e);
		}
	}

	private ICommandShell createCommandShell() throws LinuxManagerException {
		try {
			return this.linuxManager.getIpNetworkManager().getCommandShell(this.ipHost, this.ipHost.getDefaultCredentials()); 
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
			return this.ipHost.getDefaultCredentials();
		} catch (IpNetworkManagerException e) {
			throw new LinuxManagerException("Unable to obtain default credentials for linux host tagged " + this.tag, e);
		}
	}

	@Override
	public @NotNull ICommandShell getCommandShell() throws LinuxManagerException {
		return this.commandShell;
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
