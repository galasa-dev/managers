/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix.ssh.manager.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosunix.IZosUNIXCommand;
import dev.galasa.zosunix.ZosUNIXCommandException;

/**
 * Implementation of {@link IZosConsoleCommand} using zOS/MF
 *
 */
public class ZosUNIXCommandImpl implements IZosUNIXCommand {

    private IZosImage image;
    private String command;    
    private String commandResponse;
    private IIpNetworkManagerSpi ipNetworkManager;
    private ICommandShell commandShell;

    public ZosUNIXCommandImpl(@NotNull String command, IZosImage image) throws ZosUNIXCommandException {
        this.image = image;
        this.command = command;
        this.ipNetworkManager = ZosUNIXCommandManagerImpl.ipNetworkManager;
        IIpHost host = image.getIpHost();
        ICredentials credentials;
        try {
            credentials = image.getDefaultCredentials();
        } catch (ZosManagerException e) {
            throw new ZosUNIXCommandException("Unable to get default crdentials for image " + image.getImageID(), e);
        }
        try {
            this.commandShell = ipNetworkManager.getCommandShell(host, credentials);
            this.commandShell.reportResultStrings(true);
        } catch (IpNetworkManagerException e) {
            throw new ZosUNIXCommandException("Unable to get IP Network Command Shell on image" + image.getImageID(), e);
        }
    }

    public IZosUNIXCommand issueCommand() throws ZosUNIXCommandException {
        try {            
            this.commandResponse = commandShell.issueCommand(this.command);
        } catch (IpNetworkManagerException e) {
            throw new ZosUNIXCommandException("Unable to issue command zOS UNIX Command", e);
        }
        return this;
    }

    public IZosUNIXCommand issueCommand(long timeout) throws ZosUNIXCommandException {
        try {            
            this.commandResponse = commandShell.issueCommand(this.command, timeout);
        } catch (IpNetworkManagerException e) {
            throw new ZosUNIXCommandException("Unable to issue command zOS UNIX Command", e);
        }
        return this;
    }

    @Override
    public String getResponse() throws ZosUNIXCommandException {
        return this.commandResponse;
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public String toString() {
        String resp = this.commandResponse != null ? " RESPONSE:\n " + this.commandResponse : "";
        return "COMMAND=" + this.command + (this.image != null ? " IMAGE=" +  this.image.getImageID() : "") + resp;
    }
}
