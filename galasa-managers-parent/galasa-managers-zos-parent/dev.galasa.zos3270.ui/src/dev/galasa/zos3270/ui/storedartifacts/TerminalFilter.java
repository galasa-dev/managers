/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.storedartifacts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import dev.galasa.eclipse.ui.run.storedartifacts.ArtifactFile;
import dev.galasa.eclipse.ui.run.storedartifacts.ArtifactFolder;
import dev.galasa.eclipse.ui.run.storedartifacts.IArtifact;
import dev.galasa.eclipse.ui.run.storedartifacts.IStoredArtifactsFilter;
import dev.galasa.zos3270.ui.Zos3270Activator;

public class TerminalFilter implements IStoredArtifactsFilter {

    @Override
    public void filter(String runId, IArtifact rootArtifact) {

        try {
            //*** Locate the terminals folder if it exists
            IArtifact zos3270Folder = rootArtifact.getChild("zos3270");
            if (zos3270Folder == null) {
                return;
            }
            IArtifact zos3270TerminalsFolder = zos3270Folder.getChild("terminals");
            if (zos3270TerminalsFolder == null) {
                return;
            }

            IArtifact[] terminalFolders = zos3270TerminalsFolder.getChildren();
            for(IArtifact terminalFolder : terminalFolders) {
                if (terminalFolder instanceof ArtifactFolder) {
                    ArrayList<ArtifactFile> terminalImages = new ArrayList<>();

                    boolean foundTerminalImages = false;
                    for(IArtifact oImage : terminalFolder.getChildren()) {
                        if (oImage instanceof ArtifactFile) {
                            ArtifactFile imageFile = (ArtifactFile) oImage;

                            Path imagePath = imageFile.getPath();
                            Map<String, Object> attrs = Files.readAttributes(imagePath, "ras:contentType");

                            String contentType = (String) attrs.get("ras:contentType");
                            if ("application/zos3270terminal".equals(contentType)) {
                                terminalImages.add(imageFile);
                                foundTerminalImages = true;
                            }
                        }
                    }

                    if (foundTerminalImages) {
                        TerminalArtifact ta = new TerminalArtifact(runId, terminalFolder.getName(), terminalImages);

                        ((ArtifactFolder)zos3270TerminalsFolder).replaceArtifact(terminalFolder, ta);
                    }
                }
            }
        } catch(Exception e) {
            Zos3270Activator.log(e);
        }

    }

}
