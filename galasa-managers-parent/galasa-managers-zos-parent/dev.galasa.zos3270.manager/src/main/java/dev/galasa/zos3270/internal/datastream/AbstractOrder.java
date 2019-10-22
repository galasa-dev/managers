/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.internal.datastream;

public abstract class AbstractOrder {

    @Override
    public String toString() {
        return "Ignore SQ";
    }

    public abstract byte[] getBytes();

}
