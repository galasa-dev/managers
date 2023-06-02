/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.spi;

import dev.galasa.zos3270.Color;
import dev.galasa.zos3270.internal.datastream.AttributeBackgroundColor;
import dev.galasa.zos3270.internal.datastream.AttributeCharacterSet;
import dev.galasa.zos3270.internal.datastream.AttributeExtendedHighlighting;
import dev.galasa.zos3270.internal.datastream.AttributeFieldOutlining;
import dev.galasa.zos3270.internal.datastream.AttributeFieldValidation;
import dev.galasa.zos3270.internal.datastream.AttributeForegroundColor;
import dev.galasa.zos3270.internal.datastream.AttributeTransparency;

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

    @SuppressWarnings("unused")
    private AttributeFieldValidation      attributeFieldValidation      = null;
    @SuppressWarnings("unused")
    private AttributeFieldOutlining       attributeFieldOutlining       = null;
    private AttributeExtendedHighlighting attributeExtendedHighlighting = null;
    @SuppressWarnings("unused")
    private AttributeCharacterSet         attributeCharacterSet         = null;
    private AttributeForegroundColor      attributeForegroundColor     = null;
    private AttributeBackgroundColor      attributeBackgroundColor     = null;
    @SuppressWarnings("unused")
    private AttributeTransparency         attributeTransparency         = null;

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
    
    public BufferStartOfField(int position, boolean fieldProtected, boolean fieldNumeric, boolean fieldDisplay,
            boolean fieldIntenseDisplay, boolean fieldSelectorPen, boolean fieldModifed,
            AttributeExtendedHighlighting extendedHighlighting, AttributeForegroundColor foregroundColor, AttributeBackgroundColor backgroundColor) {
        this(position, fieldProtected, fieldNumeric, fieldDisplay, fieldIntenseDisplay, fieldSelectorPen, fieldModifed);
        
        this.attributeExtendedHighlighting = extendedHighlighting;
        this.attributeForegroundColor     = foregroundColor;
        this.attributeBackgroundColor     = backgroundColor;
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
    
    public Highlight getHighlight() {
        if (this.attributeExtendedHighlighting == null) {
            return null;
        }
        return this.attributeExtendedHighlighting.getHighlight();
    }
    

    /**
     * @deprecated
     * This call was deprecated in version 0.28.0 in favour of {@link #getForegroundColor}
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Colour getForegroundColour() {
        if (this.attributeForegroundColor == null) {
            return null;
        }
        return Colour.getColour(this.attributeForegroundColor.getColor());
    }

    /** 
     * @since 0.28.0
     */
    public Color getForegroundColor() {
        if (this.attributeForegroundColor == null) {
            return null;
        }
        return this.attributeForegroundColor.getColor();
    }

    /**
     * @deprecated
     * This call was deprecated in version 0.28.0 in favour of {@link #getBackgroundColor}
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Colour getBackgroundColour() {
        if (this.attributeBackgroundColor == null) {
            return null;
        }
        return Colour.getColour(this.attributeBackgroundColor.getColor());
    }

    /** 
     * @since 0.28.0
     */
    public Color getBackgroundColor() {
        if (this.attributeBackgroundColor == null) {
            return null;
        }
        return this.attributeBackgroundColor.getColor();
    }

    public AttributeExtendedHighlighting getAttributeExtendedHighlighting() {
        return this.attributeExtendedHighlighting;
    }

    /**
     * @since 0.28.0
     */
    protected AttributeForegroundColor getAttributeForegroundColor() {
        return this.attributeForegroundColor;
    }

    /**
     * @since 0.28.0
     */
    protected AttributeBackgroundColor getAttributeBackgroundColor() {
        return this.attributeBackgroundColor;
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
