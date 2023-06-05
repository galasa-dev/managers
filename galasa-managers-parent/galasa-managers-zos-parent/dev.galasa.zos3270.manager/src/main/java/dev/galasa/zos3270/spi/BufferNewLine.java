/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;

public class BufferNewLine extends BufferChar {

    public BufferNewLine() {
        super((char)0x15);
    }

    @Override
    public String toString() {
        return "newline()";
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
        return 0x15;
    }

}
