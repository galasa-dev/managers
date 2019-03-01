package io.ejat.zos3270.internal.datastream;

public abstract class QueryReply {
	
	public static final byte QUERY_REPLY = (byte)0x81;
	
	abstract public byte[] toByte();
	abstract public byte   getID();

}
