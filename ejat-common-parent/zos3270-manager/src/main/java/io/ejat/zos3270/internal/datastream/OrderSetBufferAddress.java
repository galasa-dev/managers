package io.ejat.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import io.ejat.zos3270.spi.DatastreamException;

public class OrderSetBufferAddress extends Order {
	
	public static final byte ID = 0x11;
	
	private final BufferAddress bufferAddress;
	
	public OrderSetBufferAddress(ByteBuffer buffer) throws DatastreamException {
		this.bufferAddress = new BufferAddress(buffer);
	} 
	
	@Override
	public String toString() {
		return "SBA(" + bufferAddress + ")";
	}
	
}
