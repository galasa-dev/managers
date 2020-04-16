/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso.ssh.manager.internal;

import dev.galasa.zos.IZosImage;
import dev.galasa.zostso.IZosTSOCommand;
import dev.galasa.zostso.ZosTSOCommandException;
import dev.galasa.zosunix.IZosUNIX;
import dev.galasa.zosunix.IZosUNIXCommand;
import dev.galasa.zosunix.ZosUNIXCommandException;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;

/**
 * Implementation of {@link IZosTSOCommand} using ssh
 *
 */
public class ZosTSOCommandImpl implements IZosTSOCommand {

    private IZosImage image;
    private String command;
    private IZosUNIX zosUnix;
    private IZosUNIXCommand zosUnixcommand;

    public ZosTSOCommandImpl(String command, IZosImage image) throws ZosTSOCommandException {
        this.image = image;
        this.command = command;
        try {
            this.zosUnix = ZosTSOCommandManagerImpl.zosUnixCommandManager.getZosUNIX(image);
        } catch (ZosUNIXCommandManagerException e) {
            throw new ZosTSOCommandException("Unable to get zOS UNIX Command Manager", e);
        }
    }

    public ZosTSOCommandImpl issueCommand() throws ZosTSOCommandException {
        try {
            this.zosUnixcommand = this.zosUnix.issueCommand(buildCommand());
        } catch (ZosUNIXCommandException e) {
            throw new ZosTSOCommandException("Unable to issue zOS TSO Command", e);
        }
        return this;
    }

    public ZosTSOCommandImpl issueCommand(long timeout) throws ZosTSOCommandException {
        try {
            this.zosUnixcommand = this.zosUnix.issueCommand(buildCommand(), timeout);
        } catch (ZosUNIXCommandException e) {
            throw new ZosTSOCommandException("Unable to issue zOS TSO Command", e);
        }
        return this;
    }

    @Override
    public String getResponse() throws ZosTSOCommandException {
        try {
            return this.zosUnixcommand.getResponse();
        } catch (ZosUNIXCommandException e) {
            throw new ZosTSOCommandException("Unable to get zOS TSO Command response", e);
        }
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public String toString() {
        String resp = this.zosUnixcommand != null ? " RESPONSE:\n " + this.zosUnixcommand : "";
        return "COMMAND=" + this.command + (this.image != null ? " IMAGE=" +  this.image.getImageID() : "") + resp;
    }

    private String buildCommand() {
        StringBuilder builtCommand = new StringBuilder();
        builtCommand.append("tsocmd ");
        if (!this.command.startsWith("\"")) {
            builtCommand.append("\"");
        }
        builtCommand.append(this.command);
        if (!this.command.endsWith("\"")) {
            builtCommand.append("\"");
        }
        return builtCommand.toString();
    }
}
