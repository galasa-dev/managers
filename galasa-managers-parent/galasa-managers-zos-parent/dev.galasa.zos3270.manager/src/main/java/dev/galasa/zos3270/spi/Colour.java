/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.spi;

import dev.galasa.zos3270.Color;

/**
 * @deprecated
 * This enum is replaced by the {@link dev.galasa.zos3270.Color} enumeration in version 0.28.0
 * So color is exposed on the API and not just the SPI packages.
 * This enum will be removed in a future release.
 */
@Deprecated(since = "0.28.0", forRemoval = true)
public enum Colour {
    
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
    
    Colour(byte code, char letter) {
        this.code   = code;
        this.letter = letter;
    }
    
    public byte getCode() {
        return this.code;
    }
    
    public char getLetter() {
        return this.letter;
    }
    
    /**
     * 
     * @param code
     * @return
     * @throws DatastreamException
     * 
     * @deprecated
     * This call is deprecated in version 0.28.0 in favour of {@link dev.galasa.zos3270.Color#getColor(byte)}
     */
    @Deprecated()
    public static Colour getColour(byte code) throws DatastreamException {
        for(Colour colour : Colour.values()) {
            if (colour.code == code) {
                return colour;
            }
        }
        
        throw new DatastreamException("Unrecognised colour code - " + code);
    }

    public static Colour getColour(Color color)  {
        Colour result = null;
        if (color!=null) {

            byte code = color.getCode();
            for(Colour colour : Colour.values()) {
                if (colour.code == code) {
                    result = colour;
                    break;
                }
            }
        }

        // We can't throw an exception in this case. All the Color values should be mapped
        // to something in this Colour class. Not expecting to ever reach this point.
        return result ;
    }
}
