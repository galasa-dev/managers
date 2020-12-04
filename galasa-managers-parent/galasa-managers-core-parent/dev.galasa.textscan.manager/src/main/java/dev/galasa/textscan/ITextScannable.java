/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

import java.io.InputStream;

import dev.galasa.ManagerException;

/**
 * This interface provides a access to a text resource that can be repeatedly updated and scanned for text.
 * Examples with be a running batch job, docker container log or a linux server log.
 * 
 * It is the intention that other Managers will provide IScannable objects for use within the Text Scan Manager
 * @author Michael Baylis
 *
 */
public interface ITextScannable {
    
    /**
     * Is the scannable object fetched by inputstream, used when the text file can be so large that it may 
     * exceed the JVM heap.
     * 
     * @return true if is an inputstream
     */
    boolean isInputStream();
    
    /**
     * Used for normal logs that fit easily in a standard String
     * 
     * @return true if it is a simple String object
     */
    boolean isString();
    
    
    /**
     * A name so the this scannable can be identified in exceptions or warning messages
     * 
     * @return a string to uniquely identify the scannable
     */
    String getName();
    
    /**
     * Update/Refresh the scannable text,  likely to be a no-op if this is an inputstream scannable. 
     * 
     * @return this scannable for fluent use
     * @throws ManagerException - If the update fails
     */
    ITextScannable update() throws ManagerException;
    
    /**
     * Fetch the InputStream for the scannable
     * 
     * @return The latest inputstream
     * @throws ManagerException - If there is an error fetching the inputstream or it is not an inputstream
     */
    InputStream getInputStream() throws ManagerException;
    
    /**
     * Fetch the latest scannable text
     * 
     * @return The latest scannable text
     * @throws ManagerException if there is an error retrieving the text
     */
    String getString() throws ManagerException;

}
