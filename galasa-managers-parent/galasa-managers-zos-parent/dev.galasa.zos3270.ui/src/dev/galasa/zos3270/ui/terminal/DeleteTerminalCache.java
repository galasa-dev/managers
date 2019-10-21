/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import dev.galasa.eclipse.Activator;
import dev.galasa.zos3270.ui.Zos3270Activator;

public class DeleteTerminalCache extends Job {

    private final Path liveTerminals;

    public DeleteTerminalCache(String name, Path liveTerminals) {
        super(name);
        this.liveTerminals = liveTerminals;
    }

    @Override
    protected IStatus run(IProgressMonitor arg0) {

        try {
            if (Files.exists(liveTerminals)) {
                //*** First we need to MOVE the cache library incase the test is still running
                //*** trying to delete the directory as the test is writing wont work.
                //*** Needs move to base cache path to avoid the live terminal monitor recreating the terminal view
                Path deletePath = Activator.getCachePath().resolve(UUID.randomUUID().toString());
                Files.move(liveTerminals, deletePath);
                Activator.deleteCache(deletePath);
            }
        } catch (IOException e) {
            return new Status(Status.ERROR, Zos3270Activator.PLUGIN_ID, "Failed", e);
        }

        return new Status(Status.OK, Zos3270Activator.PLUGIN_ID, "Live Terminal deleted");
    }

}
