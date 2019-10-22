/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270;

public class FieldNotFoundException extends Zos3270Exception {
    private static final long serialVersionUID = 1L;

    public FieldNotFoundException() {
    }

    public FieldNotFoundException(String message) {
        super(message);
    }

    public FieldNotFoundException(Throwable cause) {
        super(cause);
    }

    public FieldNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
