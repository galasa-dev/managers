/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2020.
 */
package dev.galasa.zos3270.spi;

public interface IBufferHolder {

    String getStringWithoutNulls();
    
    char getChar();

}
