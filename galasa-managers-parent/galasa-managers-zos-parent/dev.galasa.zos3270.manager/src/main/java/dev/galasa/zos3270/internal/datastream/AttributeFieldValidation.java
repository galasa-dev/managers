package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeFieldValidation implements IAttribute {
	
	public final static byte ATTRIBUTE_ID = -63;
	
	private final byte validation;
	
	public AttributeFieldValidation(ByteBuffer buffer) {
		this.validation = buffer.get();
	}

}
