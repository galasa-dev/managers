/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.java;

import dev.galasa.ManagerException;

public class JavaManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public JavaManagerException() {
    }

    public JavaManagerException(String message) {
        super(message);
    }

    public JavaManagerException(Throwable cause) {
        super(cause);
    }

    public JavaManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JavaManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
