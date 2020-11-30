/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

import dev.galasa.zos3270.ITerminal;

public interface ICicsTerminal extends ITerminal {

    ICicsRegion getCicsRegion();

    boolean connectToCicsRegion() throws CicstsManagerException;
    
    ICicsTerminal resetAndClear() throws CicstsManagerException;

}