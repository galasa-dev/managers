/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2020.
 */
package dev.galasa.zos3270.spi;

/**
 * Create a Start of Field position, represents to SF order
 * 
 * @author Michael Baylis
 *
 */
public class BufferStartOfField implements IBufferHolder {

    private final boolean fieldProtected;
    private final boolean fieldNumeric;
    private final boolean fieldDisplay;
    private final boolean fieldIntenseDisplay;
    private final boolean fieldSelectorPen;
    private boolean       fieldModifed;

    /**
     * Create the start of a field
     * 
     * @param position - The buffer position the field is to start
     */
    public BufferStartOfField(int position, boolean fieldProtected, boolean fieldNumeric, boolean fieldDisplay,
            boolean fieldIntenseDisplay, boolean fieldSelectorPen, boolean fieldModifed) {
        this.fieldProtected = fieldProtected;
        this.fieldNumeric = fieldNumeric;
        this.fieldDisplay = fieldDisplay;
        this.fieldIntenseDisplay = fieldIntenseDisplay;
        this.fieldSelectorPen = fieldSelectorPen;
        this.fieldModifed = fieldModifed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.zos3270.internal.terminal.fields.Field#toString()
     */
    @Override
    public String toString() {
        return "StartOfField(" + super.toString() + ")";
    }

    public boolean isProtected() {
        return fieldProtected;
    }

    public boolean isNumeric() {
        return fieldNumeric;
    }

    public boolean isDisplay() {
        return fieldDisplay;
    }

    public boolean isIntenseDisplay() {
        return fieldIntenseDisplay;
    }

    public boolean isSelectorPen() {
        return fieldSelectorPen;
    }

    public boolean isFieldModifed() {
        return fieldModifed;
    }

    public void setFieldModified() {
        this.fieldModifed = true;
    }
    
    public void clearFieldModified() {
        this.fieldModifed = false;
    }

    @Override
    public String getStringWithoutNulls() {
        return " ";
    }
    
    @Override
    public char getChar() {
        return ' ';
    }


}
