/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

public class CicsJvmprofileResourceException extends CicsResourceManagerException {
    private static final long serialVersionUID = 1L;

    public CicsJvmprofileResourceException() {
    }

    public CicsJvmprofileResourceException(String message) {
        super(message);
    }

    public CicsJvmprofileResourceException(Throwable cause) {
        super(cause);
    }

    public CicsJvmprofileResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicsJvmprofileResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
