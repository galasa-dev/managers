/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.Screen;

public class QueryReplyImplicitPartition extends QueryReply {
	
	private static final byte IMPLICIT_PARTITION = (byte)0xa6;
	
	private final int cellX;
	private final int cellY;
	
	
	public QueryReplyImplicitPartition(Screen screen) {
		this.cellX = screen.getNoOfColumns();
		this.cellY = screen.getNoOfRows();
	}

	@Override
	public byte[] toByte() {
		ByteBuffer buffer = ByteBuffer.allocate(17);
		buffer.putShort((short) 17);
		buffer.put(QueryReply.QUERY_REPLY);
		buffer.put(IMPLICIT_PARTITION);
		buffer.putShort((short) 0);  //*** Flags
		
        buffer.put((byte)11);  //*** Length of self defining parameter
        buffer.put((byte)1);   //*** Implicit Partition Size
        buffer.put((byte)0);   //*** Flags
        buffer.putShort((short)cellX);
        buffer.putShort((short)cellY);
        buffer.putShort((short)cellX);
        buffer.putShort((short)cellY);
		return buffer.array();
	}
	
	@Override
	public byte getID() {
		return IMPLICIT_PARTITION;
	}
	
}
