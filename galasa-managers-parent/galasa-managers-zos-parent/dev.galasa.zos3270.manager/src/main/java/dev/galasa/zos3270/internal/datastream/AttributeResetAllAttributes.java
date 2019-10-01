package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeResetAllAttributes implements IAttribute {
	
	public final static byte ATTRIBUTE_ID = 0x00;
	
	private final byte reset;
	
	public AttributeResetAllAttributes(ByteBuffer buffer) {
		this.reset = buffer.get();
	}

}
