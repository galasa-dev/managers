/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci.internal;

import dev.galasa.cicsts.ceci.CECIException;
import dev.galasa.cicsts.ceci.IResponseOutputValue;

public class ResponseOutputValueImpl implements IResponseOutputValue {

    private Object value;
    public ResponseOutputValueImpl(Object value) {
        this.value = value;
    }
    
    @Override
    public String getTextValue() {
        if (value.getClass().isArray()) {
            Object[] objects = (Object[]) value;
            return (String) objects[0];
        }
        return value.toString();
    }
    
    @Override
    public char[] getHexValue() {
        if (value.getClass().isArray()) {
            Object[] objects = (Object[]) value;
            return ((String) objects[1]).toCharArray();
        }
        return ((String) value).toCharArray();
    }

    @Override
    public int getIntValue() throws CECIException {
        if (!(value instanceof Integer)) {
            throw new CECIException("Value is " + value.getClass().getName() + " type");
        }
        return (int) value;
    }

    @Override
    public long getLongValue() throws CECIException {
        if (!(value instanceof Long)) {
            throw new CECIException("Value is " + value.getClass().getName() + " type");
        }
        return (long) value;
    }
    
    @Override
    public String toString() {
        return getTextValue();
    }

    @Override
    public boolean isArray() {
        return value.getClass().isArray();
    }
}
