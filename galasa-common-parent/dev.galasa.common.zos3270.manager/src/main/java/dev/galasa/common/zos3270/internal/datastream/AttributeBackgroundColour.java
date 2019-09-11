package dev.galasa.common.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeBackgroundColour implements IAttribute {
	
	public final static byte ATTRIBUTE_ID = 0x45;
	
	private final byte colour;
	
	public AttributeBackgroundColour(ByteBuffer buffer) {
		this.colour = buffer.get();
	}

}
