package io.ejat.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.ejat.zos3270.spi.DatastreamException;

public class OrderRepeatToAddress extends Order {
	
	private static final Charset ebcdic = Charset.forName("Cp037");
	
	public static final byte ID = 0x3c;
	
	private final BufferAddress bufferAddress;
	
	private final char repeatChar;
	
	public OrderRepeatToAddress(ByteBuffer buffer) throws DatastreamException {
		this.bufferAddress = new BufferAddress(buffer);
		
		byte[] charByte = new byte[] {buffer.get()};
		repeatChar = ebcdic.decode(ByteBuffer.wrap(charByte)).array()[0];
	} 
	
	@Override
	public String toString() {
		return "RA(" + repeatChar + "," + bufferAddress + ")";
	}
	
}
