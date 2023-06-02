/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270;

import dev.galasa.zos3270.spi.DatastreamException;

/**
 * @since 0.28.0
 */
public enum Color {
    
    DEFAULT  ((byte)0x0, 'd'),
    BLUE     ((byte)0xf1, 'b'),
    RED      ((byte)0xf2, 'r'),
    PINK     ((byte)0xf3, 'p'),
    GREEN    ((byte)0xf4, 'g'),
    TURQUOISE((byte)0xf5, 't'),
    YELLOW   ((byte)0xf6, 'y'),
    NEUTRAL  ((byte)0xf7, 'n');
    
    private final byte code;
    private final char letter;
    
    Color(byte code, char letter) {
        this.code   = code;
        this.letter = letter;
    }
    
    public byte getCode() {
        return this.code;
    }
    
    public char getLetter() {
        return this.letter;
    }
    
    public static Color getColor(byte code) throws DatastreamException {
        for(Color colour : Color.values()) {
            if (colour.code == code) {
                return colour;
            }
        }
        
        throw new DatastreamException("Unrecognised colour code - " + code);
    }

}
