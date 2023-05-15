/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.Color;

public class QueryReplyColor extends AbstractQueryReply {

    private static final byte COLOR = (byte) 0x86;

    @Override
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(22);
        buffer.putShort((short) 22);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(COLOR);
        buffer.put((byte) 0); // *** Flags
        
        buffer.put((byte)8); // Number of color combinations
        
        putColor(buffer, Color.DEFAULT.getCode(),   Color.GREEN.getCode());
        putColor(buffer, Color.BLUE.getCode(),      Color.BLUE.getCode());
        putColor(buffer, Color.RED.getCode(),       Color.RED.getCode());
        putColor(buffer, Color.PINK.getCode(),      Color.PINK.getCode());
        putColor(buffer, Color.GREEN.getCode(),     Color.GREEN.getCode());
        putColor(buffer, Color.TURQUOISE.getCode(), Color.TURQUOISE.getCode());
        putColor(buffer, Color.YELLOW.getCode(),    Color.YELLOW.getCode());
        putColor(buffer, Color.NEUTRAL.getCode(),   Color.NEUTRAL.getCode());

        return buffer.array();
    }

    private void putColor(ByteBuffer buffer, byte colorAcceptedValue, byte colorIdentity) {
        buffer.put(colorAcceptedValue);
        buffer.put(colorIdentity);
        
        return;
    }

    @Override
    public byte getID() {
        return COLOR;
    }

}
