/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci;

import dev.galasa.zos.ZosManagerException;

public class CECIManagerException extends ZosManagerException {
    private static final long serialVersionUID = 1L;

    public CECIManagerException() {
    }

    public CECIManagerException(String message) {
        super(message);
    }

    public CECIManagerException(Throwable cause) {
        super(cause);
    }

    public CECIManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CECIManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
