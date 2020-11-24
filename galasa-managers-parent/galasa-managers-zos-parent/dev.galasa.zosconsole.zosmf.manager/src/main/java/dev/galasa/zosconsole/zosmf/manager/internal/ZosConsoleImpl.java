/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;

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
            zosConsoleCommand = new ZosConsoleCommandImpl(command, consoleName(consoleName), this.image);
            this.zosConsoleCommands.add(zosConsoleCommand);
        } catch (ZosConsoleManagerException e) {
            throw new ZosConsoleException("Unable to issue console command", e);
        }
        
        return zosConsoleCommand.issueCommand();
    }

    protected String consoleName(String consoleName) throws ZosConsoleException {
        if (consoleName == null) {
            try {
                ICredentials creds = image.getDefaultCredentials();
                if (!(creds instanceof ICredentialsUsernamePassword)) {
                    throw new ZosConsoleException("Unable to get the run username for image "  + image.getImageID());
                }
                return ((ICredentialsUsernamePassword) creds).getUsername();
            } catch (ZosManagerException e) {
                throw new ZosConsoleException("Unable to get the run username for image "  + image.getImageID());
            }
        }
        if (consoleName.length() < 2 || consoleName.length() > 8) {
            throw new ZosConsoleException("Invalid console name \"" + consoleName + "\" must be between 2 and 8 charaters long");
        }
        return consoleName;
    }
    
    @Override
    public String toString() {
    	return this.image.getImageID();
    }
}
