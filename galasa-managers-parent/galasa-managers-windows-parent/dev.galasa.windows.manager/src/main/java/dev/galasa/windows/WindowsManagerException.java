/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.windows;

import dev.galasa.ManagerException;

public class WindowsManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public WindowsManagerException() {
    }

    public WindowsManagerException(String message) {
        super(message);
    }

    public WindowsManagerException(Throwable cause) {
        super(cause);
    }

    public WindowsManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public WindowsManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
