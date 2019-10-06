/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeFieldValidation implements IAttribute {

    public static final byte ATTRIBUTE_ID = -63;

    private final byte       validation;

    public AttributeFieldValidation(ByteBuffer buffer) {
        this.validation = buffer.get();
    }
    
    public byte getValidation() {
        return validation;
    }

}
