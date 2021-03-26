/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

public class CicsBundleResourceException extends CicsResourceManagerException {
    private static final long serialVersionUID = 1L;

    public CicsBundleResourceException() {
    }

    public CicsBundleResourceException(String message) {
        super(message);
    }

    public CicsBundleResourceException(Throwable cause) {
        super(cause);
    }

    public CicsBundleResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicsBundleResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
