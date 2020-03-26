/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.selenium;

import dev.galasa.ManagerException;

public class SeleniumManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public SeleniumManagerException() {
    }

    public SeleniumManagerException(String message) {
        super(message);
    }

    public SeleniumManagerException(Throwable cause) {
        super(cause);
    }

    public SeleniumManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeleniumManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
