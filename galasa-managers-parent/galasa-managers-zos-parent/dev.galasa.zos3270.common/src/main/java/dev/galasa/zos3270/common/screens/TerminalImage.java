/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation.
 *******************************************************************************/
package dev.galasa.zos3270.common.screens;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Pojo to represent a screen image as received or sent from the client. Inbound
 * means received by the client.
 * 
 * @author Michael Baylis
 *
 */
public class TerminalImage {

    private final long                sequence;
    private final String              id;
    private final boolean             inbound;                                // *** Inbound means from server -> client
    private final String              type;

    private final TerminalSize        imageSize;

    private final List<TerminalField> fields = new ArrayList<TerminalField>();

    /**
     * Constructor
     * 
     * @param sequence The sequence of the image, for the whole terminal interaction
     * @param id Id of this image
     * @param inbound inbound = true if received by the client
     * @param type Type of interaction, interactions can be labels so can be filtered
     * @param imageSize The size of the image if different to the default
     */
    public TerminalImage(long sequence, @NotNull String id, boolean inbound, String type, TerminalSize imageSize) {
        this.sequence = sequence;
        this.id = id;
        this.inbound = inbound;
        this.type = type;
        this.imageSize = imageSize;
    }

    /**
     * Fetch the sequence number
     * 
     * @return sequence number
     */
    public long getSequence() {
        return sequence;
    }

    /**
     * Fetch the id
     * 
     * @return the id
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Is inbound to the client.  false means being sent from the client
     * 
     * @return inbound
     */
    public boolean isInbound() {
        return inbound;
    }

    /**
     * Return the image size,  if null, then it is the default size
     * 
     * @return the terminal size if not default
     */
    public TerminalSize getImageSize() {
        return imageSize;
    }

    /**
     * Fetch the fields
     * 
     * @return Fields
     */
    public List<TerminalField> getFields() {
        return fields;
    }

    /**
     * Get the filtering type of the image
     * 
     * @return filtering type
     */
    public String getType() {
        return type;
    }

}
