/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

public class CicsResourceException extends CicsResourceManagerException {
    private static final long serialVersionUID = 1L;

    public CicsResourceException() {
    }

    public CicsResourceException(String message) {
        super(message);
    }

    public CicsResourceException(Throwable cause) {
        super(cause);
    }

    public CicsResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicsResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
