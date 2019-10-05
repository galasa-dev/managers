/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.common.screens;

/**
 * Pojo to contain the onscreen contents.  May contain nulls.
 * 
 * @author Michael Baylis
 *
 */
public class FieldContents {

    private Character[] chars;

    /**
     * Constructor
     * 
     * @param chars the chars in the relevant places, may contain nulls
     */
    public FieldContents(Character[] chars) {
        this.chars = chars;
    }

    /**
     * Get the field characters
     * 
     * @return the chars
     */
    public Character[] getChars() {
        return this.chars;
    }

}
