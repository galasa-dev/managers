/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeForegroundColour implements IAttribute {

    public static final byte ATTRIBUTE_ID = 0x42;

    private final byte       colour;

    public AttributeForegroundColour(ByteBuffer buffer) {
        this.colour = buffer.get();
    }

    public byte getColour() {
        return colour;
    }

}
