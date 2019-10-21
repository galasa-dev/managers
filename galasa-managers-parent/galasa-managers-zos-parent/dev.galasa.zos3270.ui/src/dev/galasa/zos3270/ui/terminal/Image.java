/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;

public class Image {

    private final int imageSequence;
    private final Images images;

    private TerminalImage terminalImage;

    private final TerminalSize size;

    public Image(TerminalImage terminalImage, Images images) {
        this.images = images;
        this.imageSequence = (int) terminalImage.getSequence() - 1;

        this.images.addImage(this);
        this.size = terminalImage.getImageSize();
    }

    public TerminalImage getTerminalImage() {
        return this.terminalImage;
    }

    public Images getImages() {
        return this.images;
    }

    public void clearCache() {
        this.terminalImage = null;
    }

    public int getSequence() {
        return this.imageSequence;
    }

    public boolean updateImage(TerminalImage terminalImage) {
        if (this.terminalImage != null) {
            return false;
        }
        this.terminalImage = terminalImage;
        return true;
    }

    public TerminalSize getSize() {
        if (this.size == null) {
            return this.images.getSize();
        }
        return size;
    }

}
