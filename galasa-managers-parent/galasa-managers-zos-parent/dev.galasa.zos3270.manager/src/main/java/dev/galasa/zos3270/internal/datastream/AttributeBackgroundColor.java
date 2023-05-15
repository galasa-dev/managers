/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.Color;
import dev.galasa.zos3270.spi.DatastreamException;

public class AttributeBackgroundColor implements IAttribute {

    public static final byte ATTRIBUTE_ID = 0x45;

    private final Color       color;

    public AttributeBackgroundColor(ByteBuffer buffer) throws DatastreamException {
        byte code = buffer.get();
        
        this.color = Color.getColor(code);
    }

    public Color getColor() {
        return color;
    }

}
