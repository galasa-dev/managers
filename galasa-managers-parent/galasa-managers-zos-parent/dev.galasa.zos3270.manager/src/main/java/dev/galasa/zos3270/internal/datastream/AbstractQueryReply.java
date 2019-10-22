/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.internal.datastream;

public abstract class AbstractQueryReply {

    public static final byte QUERY_REPLY = (byte) 0x81;

    public abstract byte[] toByte();

    public abstract byte getID();

}
