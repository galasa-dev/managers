/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.util.List;

public class QueryReplySummary extends QueryReply {
	
	private static final byte SUMMARY = (byte)0x80;
	
	private final List<QueryReply> replies;
	
	public QueryReplySummary(List<QueryReply> replies) {
		this.replies = replies;
	}

	@Override
	public byte[] toByte() {
		int length = 5 + replies.size();
		
		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.putShort((short)length);
		buffer.put(QueryReply.QUERY_REPLY);
		buffer.put(SUMMARY);
		
		buffer.put(SUMMARY);
		for(QueryReply reply : replies) {
			buffer.put(reply.getID());
		}
		return buffer.array();
	}
	
	@Override
	public byte getID() {
		return SUMMARY;
	}
	
}
