package dev.galasa.common.ipnetwork.spi;

import java.nio.file.FileSystem;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.common.ipnetwork.ICommandShell;
import dev.galasa.common.ipnetwork.IIpHost;
import dev.galasa.common.ipnetwork.IpNetworkManagerException;

public interface IIpNetworkManagerSpi {

	@NotNull
	ICommandShell getCommandShell(IIpHost host, ICredentials credentials) throws IpNetworkManagerException;

	@NotNull
	FileSystem getFileSystem(IIpHost host) throws IpNetworkManagerException;

}
