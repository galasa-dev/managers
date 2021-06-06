/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

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
        
        putColor(buffer, (byte)0, (byte)0xf4);
        putColor(buffer, (byte)0xf1, (byte)0xf1);
        putColor(buffer, (byte)0xf2, (byte)0xf2);
        putColor(buffer, (byte)0xf3, (byte)0xf3);
        putColor(buffer, (byte)0xf4, (byte)0xf4);
        putColor(buffer, (byte)0xf5, (byte)0xf5);
        putColor(buffer, (byte)0xf6, (byte)0xf6);
        putColor(buffer, (byte)0xf7, (byte)0xf7);

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
