/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import dev.galasa.zos3270.ui.Zos3270Activator;

public class LoadImagesJob extends Job {

    private final TerminalView    view;
    private final Path            cachePath;
    private final ArrayList<Path> imagePaths;

    public LoadImagesJob(TerminalView view, Path cachePath, ArrayList<Path> imagePaths) {
        super("Load terminal images");

        this.view       = view;
        this.cachePath  = cachePath;
        this.imagePaths = imagePaths;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        try {
            ArrayList<Path> cachedImages = new ArrayList<>();

            
            for(Path remotePath : imagePaths) {
                String fileName = remotePath.getFileName().toString();
                Path cachedCopy = cachePath.resolve(fileName);
                Files.copy(remotePath, cachedCopy);
                cachedImages.add(cachedCopy);
            }
            
            
            //*** Rebuild the Image Index
            for(Path path : cachedImages) {
                view.addTerminalImageFile(path, null);
            }
            
            view.loadComplete();
            view.updateUI();
        } catch (Exception e) {
            return new Status(Status.ERROR, Zos3270Activator.PLUGIN_ID, "Failed", e);
        }

        return new Status(Status.OK, Zos3270Activator.PLUGIN_ID, "Terminal Images Loaded");
    }

}
