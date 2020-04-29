/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix.ssh.manager.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosunix.IZosUNIX;
import dev.galasa.zosunix.IZosUNIXCommand;
import dev.galasa.zosunix.ZosUNIXCommandException;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;

/**
 * Implementation of {@link IZosUNIX} using ssh
 *
 */
public class ZosUNIXImpl implements IZosUNIX {

    private List<ZosUNIXCommandImpl> zosUNIXCommands = new ArrayList<>();
    private IZosImage image;
    
    public ZosUNIXImpl(IZosImage image) {
        this.image = image;
    }

    @Override
    public @NotNull IZosUNIXCommand issueCommand(@NotNull String command) throws ZosUNIXCommandException {
        return newZosUNIXCommand(command).issueCommand();
    }

    @Override
    public @NotNull IZosUNIXCommand issueCommand(@NotNull String command, long timeout) throws ZosUNIXCommandException {        
        return newZosUNIXCommand(command).issueCommand(timeout);
    }

    protected ZosUNIXCommandImpl newZosUNIXCommand(String command) throws ZosUNIXCommandException {
        ZosUNIXCommandImpl zosUNIXCommand;
        
        try {
            zosUNIXCommand = new ZosUNIXCommandImpl(command, this.image);
            this.zosUNIXCommands.add(zosUNIXCommand);
        } catch (ZosUNIXCommandManagerException e) {
            throw new ZosUNIXCommandException("Unable to issue command zOS UNIX Command", e);
        }
        return zosUNIXCommand;
    }
}
