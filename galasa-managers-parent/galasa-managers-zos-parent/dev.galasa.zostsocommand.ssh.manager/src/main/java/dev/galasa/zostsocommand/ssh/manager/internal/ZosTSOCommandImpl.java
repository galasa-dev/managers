/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostsocommand.ssh.manager.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandException;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;
import dev.galasa.zosunixcommand.ZosUNIXCommandManagerException;

/**
 * Implementation of {@link IZosTSOCommand} using ssh
 *
 */
public class ZosTSOCommandImpl implements IZosTSOCommand {

    private IZosUNIXCommand zosUnixCommand;
    
    public ZosTSOCommandImpl(IZosImage image) throws ZosTSOCommandManagerException {
        try {
            this.zosUnixCommand = ZosTSOCommandManagerImpl.zosUnixCommandManager.getZosUNIXCommand(image);
        } catch (ZosUNIXCommandManagerException e) {
            throw new ZosTSOCommandException("Unable to get zOS UNIX Command instance", e);
        }
    }

    @Override
    public String issueCommand(@NotNull String command) throws ZosTSOCommandException {
        String commandResponse;
        try {
            commandResponse = this.zosUnixCommand.issueCommand(buildCommand(command));
        } catch (ZosUNIXCommandException e) {
            throw new ZosTSOCommandException("Unable to issue zOS TSO Command", e);
        }
        return commandResponse;
    }

    @Override
    public String issueCommand(@NotNull String command, long timeout) throws ZosTSOCommandException {
        String commandResponse;
        try {
            commandResponse = this.zosUnixCommand.issueCommand(buildCommand(command), timeout);
        } catch (ZosUNIXCommandException e) {
            throw new ZosTSOCommandException("Unable to issue zOS TSO Command", e);
        }
        return commandResponse;
    }

    protected String buildCommand(String command) {
        StringBuilder builtCommand = new StringBuilder();
        builtCommand.append("tsocmd ");
        if (!command.startsWith("\"")) {
            builtCommand.append("\"");
        }
        builtCommand.append(command);
        if (!command.endsWith("\"")) {
            builtCommand.append("\"");
        }
        return builtCommand.toString();
    }
}
