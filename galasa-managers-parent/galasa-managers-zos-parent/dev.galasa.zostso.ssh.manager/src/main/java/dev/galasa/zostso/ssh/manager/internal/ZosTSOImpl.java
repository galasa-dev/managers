/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso.ssh.manager.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zostso.IZosTSO;
import dev.galasa.zostso.IZosTSOCommand;
import dev.galasa.zostso.ZosTSOCommandException;
import dev.galasa.zostso.ZosTSOCommandManagerException;

/**
 * Implementation of {@link IZosTSO} using ssh
 *
 */
public class ZosTSOImpl implements IZosTSO {

    private List<ZosTSOCommandImpl> zosTSOCommands = new ArrayList<>();
    private IZosImage image;
    
    public ZosTSOImpl(IZosImage image) {
        this.image = image;
    }

    @Override
    public @NotNull IZosTSOCommand issueCommand(@NotNull String command) throws ZosTSOCommandException {
        
        return newZosTSOCommand(command).issueCommand();
    }

    @Override
    public @NotNull IZosTSOCommand issueCommand(@NotNull String command, long timeout) throws ZosTSOCommandException {
        
        return newZosTSOCommand(command).issueCommand(timeout);
    }

    private ZosTSOCommandImpl newZosTSOCommand(String command) throws ZosTSOCommandException {
        ZosTSOCommandImpl zosTSOCommand;
        
        try {
            zosTSOCommand = new ZosTSOCommandImpl(command, this.image);
            this.zosTSOCommands.add(zosTSOCommand);
        } catch (ZosTSOCommandManagerException e) {
            throw new ZosTSOCommandException("Unable to issue TSO command", e);
        }
        return zosTSOCommand;
    }
}
