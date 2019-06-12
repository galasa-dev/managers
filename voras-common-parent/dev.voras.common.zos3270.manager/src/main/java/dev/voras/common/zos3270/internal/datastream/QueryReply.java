package dev.voras.common.zos3270.internal.datastream;

public abstract class QueryReply {
	
	public static final byte QUERY_REPLY = (byte)0x81;
	
	public abstract byte[] toByte();
	public abstract byte   getID();

}
