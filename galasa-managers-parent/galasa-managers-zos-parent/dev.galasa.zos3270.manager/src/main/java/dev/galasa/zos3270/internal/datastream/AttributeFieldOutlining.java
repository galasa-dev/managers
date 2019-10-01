package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeFieldOutlining implements IAttribute {
	
	public final static byte ATTRIBUTE_ID = -62;
	
	private final byte validation;
	
	public AttributeFieldOutlining(ByteBuffer buffer) {
		this.validation = buffer.get();
	}

}
