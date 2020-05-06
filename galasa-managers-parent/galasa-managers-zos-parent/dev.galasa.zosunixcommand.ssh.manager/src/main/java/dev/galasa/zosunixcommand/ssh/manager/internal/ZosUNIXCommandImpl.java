/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunixcommand.ssh.manager.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

/**
 * Implementation of {@link IZosUNIXCommand} using ssh
 *
 */
public class ZosUNIXCommandImpl implements IZosUNIXCommand {

    private IIpNetworkManagerSpi ipNetworkManager;
    private ICommandShell commandShell;
    
    public ZosUNIXCommandImpl(IZosImage image) throws ZosUNIXCommandException {
        this.ipNetworkManager = ZosUNIXCommandManagerImpl.ipNetworkManager;
        IIpHost host = image.getIpHost();
        ICredentials credentials;
        try {
            credentials = image.getDefaultCredentials();
        } catch (ZosManagerException e) {
            throw new ZosUNIXCommandException("Unable to get default credentials for image " + image.getImageID(), e);
        }
        try {
            this.commandShell = ipNetworkManager.getCommandShell(host, credentials);
            this.commandShell.reportResultStrings(true);
        } catch (IpNetworkManagerException e) {
            throw new ZosUNIXCommandException("Unable to get IP Network Command Shell on image " + image.getImageID(), e);
        }
    }

    @Override
    public String issueCommand(@NotNull String command) throws ZosUNIXCommandException {
        String commandResponse;
        try {            
            commandResponse = commandShell.issueCommand(command);
        } catch (IpNetworkManagerException e) {
            throw new ZosUNIXCommandException("Unable to issue zOS UNIX Command", e);
        }
        return commandResponse;
    }

    @Override
    public String issueCommand(@NotNull String command, long timeout) throws ZosUNIXCommandException {
        String commandResponse;
        try {            
            commandResponse = commandShell.issueCommand(command, timeout);
        } catch (IpNetworkManagerException e) {
            throw new ZosUNIXCommandException("Unable to issue zOS UNIX Command", e);
        }
        return commandResponse;
    }
}
