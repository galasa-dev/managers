/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zos.IZosImage;

/**
 * Implementation of {@link IZosConsole} using zOS/MF
 *
 */
public class ZosConsoleImpl implements IZosConsole {

    private List<ZosConsoleCommandImpl> zosConsoleCommands = new ArrayList<>();
    private IZosImage image;
    
    public ZosConsoleImpl(IZosImage image) {
        this.image = image;
    }

    @Override
    public @NotNull IZosConsoleCommand issueCommand(@NotNull String command) throws ZosConsoleException {
        return issueCommand(command, null);
    }

    @Override
    public @NotNull IZosConsoleCommand issueCommand(@NotNull String command, String consoleName) throws ZosConsoleException {
        ZosConsoleCommandImpl zosConsoleCommand;
        
        try {
            zosConsoleCommand = new ZosConsoleCommandImpl(command, consoleName, this.image);
            this.zosConsoleCommands.add(zosConsoleCommand);
        } catch (ZosConsoleManagerException e) {
            throw new ZosConsoleException("Unable to issue console command", e);
        }
        
        return zosConsoleCommand.issueCommand();
    }
}
