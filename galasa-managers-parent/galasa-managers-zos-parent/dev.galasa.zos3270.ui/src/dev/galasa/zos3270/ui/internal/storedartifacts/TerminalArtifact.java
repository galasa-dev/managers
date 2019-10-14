package dev.galasa.zos3270.ui.internal.storedartifacts;

import java.util.List;

import org.eclipse.ui.IWorkbenchPartSite;

import dev.galasa.eclipse.ui.run.storedartifacts.ArtifactFile;
import dev.galasa.eclipse.ui.run.storedartifacts.IArtifact;

public class TerminalArtifact implements IArtifact {

    private final String             name;
    private final List<ArtifactFile> imageArtifacts;

    public TerminalArtifact(String name, List<ArtifactFile> imageArtifacts) {
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
        System.out.println("Boo");
        
    }

}
