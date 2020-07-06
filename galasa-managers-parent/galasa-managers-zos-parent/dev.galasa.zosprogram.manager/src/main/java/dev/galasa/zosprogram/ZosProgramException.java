/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram;

public class ZosProgramException extends ZosProgramManagerException {
    private static final long serialVersionUID = 1L;

    public ZosProgramException() {
    }

    public ZosProgramException(String message) {
        super(message);
    }

    public ZosProgramException(Throwable cause) {
        super(cause);
    }

    public ZosProgramException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosProgramException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
