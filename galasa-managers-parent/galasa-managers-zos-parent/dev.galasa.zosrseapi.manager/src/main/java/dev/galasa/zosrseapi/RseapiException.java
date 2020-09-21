/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi;

public class RseapiException extends RseapiManagerException {
    private static final long serialVersionUID = 1L;

    public RseapiException() {
    }

    public RseapiException(String message) {
        super(message);
    }

    public RseapiException(Throwable cause) {
        super(cause);
    }

    public RseapiException(String message, Throwable cause) {
        super(message, cause);
    }

    public RseapiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
