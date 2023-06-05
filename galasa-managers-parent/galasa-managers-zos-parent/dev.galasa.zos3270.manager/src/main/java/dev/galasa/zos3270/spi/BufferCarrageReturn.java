/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;

public class BufferCarrageReturn extends BufferChar {

    public BufferCarrageReturn() {
        super((char)0x0d);
    }

    @Override
    public String toString() {
        return "carragereturn()";
    }

    @Override
    public String getStringWithoutNulls() {
        return " ";
    }

    @Override
    public char getChar() {
        return ' ';
    }

    @Override
    public byte getFieldEbcdic(Charset codePage) {
        return 0x0d;
    }

}
