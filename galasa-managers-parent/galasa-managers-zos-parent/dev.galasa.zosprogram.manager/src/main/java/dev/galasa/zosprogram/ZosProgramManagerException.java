/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram;

import dev.galasa.zos.ZosManagerException;

public class ZosProgramManagerException extends ZosManagerException {
    private static final long serialVersionUID = 1L;

    public ZosProgramManagerException() {
    }

    public ZosProgramManagerException(String message) {
        super(message);
    }

    public ZosProgramManagerException(Throwable cause) {
        super(cause);
    }

    public ZosProgramManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosProgramManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
