/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.common.screens;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo to represent a Field on the terminal screen.
 * 
 * @author Michael Baylis
 *
 */
public class TerminalField {

    private final int                 row;
    private final int                 column;

    private final Boolean             unformatted;
    private final Boolean             fieldProtected;
    private final Boolean             fieldNumeric;
    private final Boolean             fieldDisplay;
    private final Boolean             fieldIntenseDisplay;
    private final Boolean             fieldSelectorPen;
    private final Boolean             fieldModifed;

    private final List<FieldContents> contents = new ArrayList<>();

    /**
     * Constructor
     * 
     * @param row Start Row
     * @param column Start Column
     * @param unformatted Field is unformatted
     * @param fieldProtected Field if protected
     * @param fieldNumeric Field is Numeric
     * @param fieldDisplay Field is displayable
     * @param fieldIntenseDisplay Field is Intense
     * @param fieldSelectorPen Field can be selected
     * @param fieldModifed Field has been modified
     */
    public TerminalField(int row, int column, boolean unformatted, boolean fieldProtected, boolean fieldNumeric,
            boolean fieldDisplay, boolean fieldIntenseDisplay, boolean fieldSelectorPen, boolean fieldModifed) {
        this.row = row;
        this.column = column;
        this.unformatted = setBoolean(unformatted);
        this.fieldProtected = setBoolean(fieldProtected);
        this.fieldNumeric = setBoolean(fieldNumeric);
        this.fieldDisplay = setBoolean(fieldDisplay);
        this.fieldIntenseDisplay = setBoolean(fieldIntenseDisplay);
        this.fieldSelectorPen = setBoolean(fieldSelectorPen);
        this.fieldModifed = setBoolean(fieldModifed);
    }

    /**
     * Fetch Start row
     * 
     * @return Start row
     */
    public int getRow() {
        return row;
    }

    /**
     * Fetch Start Column
     * 
     * @return Start column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Is field unformatted.  If so,  the first position will not be a StartField
     * 
     * @return Unformatted
     */
    public boolean isUnformatted() {
        return getBoolean(unformatted);
    }

    /**
     * Is field protected
     * 
     * @return Protected
     */
    public boolean isFieldProtected() {
        return getBoolean(fieldProtected);
    }

    /**
     * Is field unformatted.  If so,  the first position will not be a StartField
     * 
     * @return Unformatted
     */
    public boolean isFieldNumeric() {
        return getBoolean(fieldNumeric);
    }

    /**
     * Is field Displayable
     * 
     * @return displayable
     */
    public boolean isFieldDisplay() {
        return getBoolean(fieldDisplay);
    }

    /**
     * Is field intense
     * 
     * @return Intense
     */
    public boolean isFieldIntenseDisplay() {
        return getBoolean(fieldIntenseDisplay);
    }

    /**
     * Is field selectable
     * 
     * @return selectable
     */
    public boolean isFieldSelectorPen() {
        return getBoolean(fieldSelectorPen);
    }

    /**
     * Has field been modified
     * 
     * @return modified
     */
    public boolean isFieldModifed() {
        return getBoolean(fieldModifed);
    }

    /**
     * Fetch the field contents
     * 
     * @return Field contents
     */
    public List<FieldContents> getContents() {
        return contents;
    }
    
    private Boolean setBoolean(boolean value) {
        if (value) {
            return Boolean.TRUE;
        }
        
        return null;
    }

    private boolean getBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        
        return value;
    }

}
