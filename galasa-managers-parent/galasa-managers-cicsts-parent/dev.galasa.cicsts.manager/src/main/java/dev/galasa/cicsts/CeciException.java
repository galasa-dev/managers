/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

public class CeciException extends CeciManagerException {
    private static final long serialVersionUID = 1L;

    public CeciException() {
    }

    public CeciException(String message) {
        super(message);
    }

    public CeciException(Throwable cause) {
        super(cause);
    }

    public CeciException(String message, Throwable cause) {
        super(message, cause);
    }

    public CeciException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
