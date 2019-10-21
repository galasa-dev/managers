/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.storedartifacts;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWorkbenchPartSite;

import dev.galasa.eclipse.ui.run.storedartifacts.ArtifactFile;
import dev.galasa.eclipse.ui.run.storedartifacts.IArtifact;
import dev.galasa.zos3270.ui.terminal.TerminalView;

public class TerminalArtifact implements IArtifact {

    private final String             runId;
    private final String             name;
    private final List<ArtifactFile> imageArtifacts;

    public TerminalArtifact(String runId, String name, List<ArtifactFile> imageArtifacts) {
        this.runId = runId;
        this.name = name;
        this.imageArtifacts = imageArtifacts;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public IArtifact[] getChildren() {
        return new IArtifact[0];
    }

    @Override
    public IArtifact getChild(String childName) {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public void doubleClick(IWorkbenchPartSite iWorkbenchPartSite) {
        ArrayList<Path> imagePaths = new ArrayList<>();
        for(ArtifactFile file : imageArtifacts) {
            imagePaths.add(file.getPath());
        }
        TerminalView.openTerminal(runId, name, imagePaths);
    }

}
