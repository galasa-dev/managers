/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso;

/**
 * Represents a zOS TSO Command
 */
public interface IZosTSOCommand {

    /**
     * Return the response from a TSO command
     * @return the response String
     */
    public String getResponse();
    
    /**
     * Return the TSO command
     * @return the TSO command String
     */
    public String getCommand();

}
