/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos3270.spi;

public class BufferFormFeed extends BufferChar {

    public BufferFormFeed() {
        super((char)0x0c);
    }

    @Override
    public String toString() {
        return "formfeed()";
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
    public byte getFieldEbcdic() {
        return 0x0c;
    }

}
