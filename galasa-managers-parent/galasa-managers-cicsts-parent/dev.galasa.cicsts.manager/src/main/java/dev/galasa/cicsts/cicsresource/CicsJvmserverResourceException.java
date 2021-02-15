/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

public class CicsJvmserverResourceException extends CicsResourceException {
    private static final long serialVersionUID = 1L;

    public CicsJvmserverResourceException() {
    }

    public CicsJvmserverResourceException(String message) {
        super(message);
    }

    public CicsJvmserverResourceException(Throwable cause) {
        super(cause);
    }

    public CicsJvmserverResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicsJvmserverResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
