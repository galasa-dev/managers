/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosprogram.ZosProgram.Language;

/** 
 * Represents a zOS Program
 */
public interface IZosProgram {
    
    /**
     * Get the zOS Program name
     * @return the program name
     */
    public String getName();
    
    /**
     * Get the zOS Program language
     * @return the program language
     */
    public Language getLanguage();
    
    /**
     * Get the zOS Program load library
     * @return the program load library
     */
    public String getLoadlib();
    
    /**
     * Get the zOS image associated with the zOS Program
     * @return the zOS image
     */
    public IZosImage getImage();
}
