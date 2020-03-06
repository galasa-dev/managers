/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci;

/**
 * Represents the value of an output field returned as the response from a CECI request. Where a field is not numeric, the value is
 * stored as both a string and a char array representing text and the hex values retrieved from the screen. In this case, the 2 representations 
 * of the value are stored in an array and {@link #isArray()} returns true.<br> 
 * e.g. the request output screen might 
 * show:<br>
 * <code>INTO('A.....')</code><br>
 * and in Hex:<<br>
 * <code>INTO(X'C13456789ABC')</code><br>
 * Both representations are available using the {@link #getTextValue()} and {@link #getHexValue()} methods respectively
 */
public interface IResponseOutputValue {

    /**
     * Returns the text value of the output field
     * @return value as a string
     */
    public String getTextValue();

    /**
     * Returns the hex value of the output field
     * @return value as a char array
     */
    public char[] getHexValue();

    /**
     * Returns an the int value of the output field
     * @return value as an int
     */
    public int getIntValue() throws CECIException;

    /**
     * Returns a long value of the output field
     * @return value as a long
     */
    public long getLongValue() throws CECIException;

    /**
     * Is the value is held in both text and hex formats 
     * @return true if value is an array
     */
    public boolean isArray();

}
