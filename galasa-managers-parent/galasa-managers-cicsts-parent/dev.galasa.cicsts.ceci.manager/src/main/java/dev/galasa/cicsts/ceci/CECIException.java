/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci;

public class CECIException extends CECIManagerException {
    private static final long serialVersionUID = 1L;

    public CECIException() {
    }

    public CECIException(String message) {
        super(message);
    }

    public CECIException(Throwable cause) {
        super(cause);
    }

    public CECIException(String message, Throwable cause) {
        super(message, cause);
    }

    public CECIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
