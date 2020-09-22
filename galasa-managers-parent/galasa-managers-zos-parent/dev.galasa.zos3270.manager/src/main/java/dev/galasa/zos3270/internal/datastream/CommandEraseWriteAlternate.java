/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos3270.internal.datastream;

public class CommandEraseWriteAlternate extends AbstractCommandCode {

    public byte[] getBytes() {
        return new byte[] { ERASE_WRITE_ALTERNATE };
    }

}
