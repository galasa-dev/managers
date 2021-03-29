/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

import dev.galasa.ManagerException;

public class ZosLibertyManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public ZosLibertyManagerException() {
    }

    public ZosLibertyManagerException(String message) {
        super(message);
    }

    public ZosLibertyManagerException(Throwable cause) {
        super(cause);
    }

    public ZosLibertyManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosLibertyManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
