/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.common.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Pojo to contain a set of terminal images. may not necessary contain all the
 * terminal images for a session.
 * 
 * These pojos are stored in a local directory for inflight tests, and in the RAS.
 * 
 * @author Michael Baylis
 *
 */
public class Terminal {

    private final String              id;
    private final long                sequence;
    private final List<TerminalImage> images = new ArrayList<>();
    private final TerminalSize        defaultSize;

    /**
     * Constructor
     * 
     * @param id Terminal ID
     * @param sequence Sequence number of this pojo for this terminal
     * @param defaultSize Default size of the terminal
     */
    public Terminal(@NotNull String id, long sequence, @NotNull TerminalSize defaultSize) {
        this.id = id;
        this.sequence = sequence;
        this.defaultSize = defaultSize;
    }

    /**
     * Fetch the ID
     * 
     * @return id
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Fetch the Images
     * 
     * @return images
     */
    public @NotNull List<TerminalImage> getImages() {
        return images;
    }

    /**
     * Add a new image to the terminal
     * 
     * @param image new image
     */
    public void addImage(@NotNull TerminalImage image) {
        getImages().add(image);
    }

    /**
     * Fetch the default size
     * 
     * @return Default size
     */
    public @NotNull TerminalSize getDefaultSize() {
        return this.defaultSize;
    }

    /**
     * Fetch the sequence number
     * 
     * @return sequence
     */
    public long getSequence() {
        return sequence;
    }
   
    
    public static void stripFalseBooleans(JsonObject json) {

        ArrayList<Entry<String, JsonElement>> entries = new ArrayList<>();
        entries.addAll(json.entrySet());

        for(Entry<String, JsonElement> entry : entries) {
            JsonElement element = entry.getValue();

            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive) element;
                if (primitive.isBoolean()) {
                    if (!primitive.getAsBoolean()) {
                        json.remove(entry.getKey());
                    }
                }
            } else if (element.isJsonObject()) {
                stripFalseBooleans((JsonObject) element);
                continue;
            } else if (element.isJsonArray()) {
                JsonArray array = (JsonArray) element;
                for(int i = 0; i < array.size(); i++) {
                    if (array.get(i).isJsonObject()) {
                        stripFalseBooleans((JsonObject) array.get(i));
                    }
                }
            }
        }
    }

}
