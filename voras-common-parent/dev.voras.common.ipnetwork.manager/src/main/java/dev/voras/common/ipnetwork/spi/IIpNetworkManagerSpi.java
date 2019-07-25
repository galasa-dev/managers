package dev.voras.common.ipnetwork.spi;

import java.nio.file.FileSystem;

import javax.validation.constraints.NotNull;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.ICommandShell;
import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.ipnetwork.IpNetworkManagerException;

public interface IIpNetworkManagerSpi {

	@NotNull
	ICommandShell getCommandShell(IIpHost host, ICredentials credentials) throws IpNetworkManagerException;

	@NotNull
	FileSystem getFileSystem(IIpHost host) throws IpNetworkManagerException;

}
