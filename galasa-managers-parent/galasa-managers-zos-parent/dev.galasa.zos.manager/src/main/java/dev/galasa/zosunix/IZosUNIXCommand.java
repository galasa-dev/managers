/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix;

/**
 * Represents a zOS UNIX Command
 *
 */
public interface IZosUNIXCommand {

    /**
     * Return the response from a UNIX command
     * @return the response String
     */
    public String getResponse();
    
    /**
     * Return the UNIX command
     * @return the UNIX command String
     */
    public String getCommand();

}
