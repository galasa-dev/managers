/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.docker.manager.ivt;

public class IvtException extends Exception {
    private static final long serialVersionUID = 1L;

    public IvtException() {
    }

    public IvtException(String message) {
        super(message);
    }

    public IvtException(Throwable cause) {
        super(cause);
    }

    public IvtException(String message, Throwable cause) {
        super(message, cause);
    }

    public IvtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
