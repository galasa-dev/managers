/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;

import dev.galasa.zos3270.common.screens.Terminal;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.ui.Zos3270Activator;

public class Images {

    private final int  sequence;
    private final Path imagesFile;

    private final Gson         gson = new Gson();

    private Terminal terminal;
    
    private final TerminalSize size;

    private final ArrayList<Image> images = new ArrayList<>();

    public Images(Path path, Terminal terminal) {
        this.imagesFile = path;
        this.sequence = (int) terminal.getSequence() - 1;
        if (terminal.getDefaultSize() == null) {
            this.size = new TerminalSize(80, 24);
        } else {
            this.size = terminal.getDefaultSize();
        }
    }

    public void addImage(Image image) {
        this.images.add(image);
    }

    public int getSequence() {
        return this.sequence;
    }

    public boolean cacheImages(int currentImageSequence) {
        boolean updatedCurrentSequence = false; 
        try {
            if (this.terminal == null) {
                try (Reader reader = new InputStreamReader(new GZIPInputStream(Files.newInputStream(imagesFile)))) {
                    this.terminal = gson.fromJson(reader, Terminal.class);
                }
            }

            for(TerminalImage ti : this.terminal.getImages()) {
                int seq = (int)ti.getSequence() - 1;
                for(Image image : images) {
                    if (image.getSequence() == seq) {
                        boolean cacheUpdated = image.updateImage(ti);
                        
                        if (seq == currentImageSequence && cacheUpdated) {
                            updatedCurrentSequence = true;
                        }
                        
                        break;
                    }
                }
            }
            
        } catch(Exception e) {
            Zos3270Activator.log(e);
        }
        return updatedCurrentSequence;
    }

    public void clearCache() {
        this.terminal = null;
        for(Image image : images) {
            image.clearCache();
        }
    }
    
    public TerminalSize getSize() {
        return size;
    }

}
