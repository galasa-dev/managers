/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso;

public class ZosTSOCommandException extends ZosTSOCommandManagerException {
    private static final long serialVersionUID = 1L;

    public ZosTSOCommandException() {
    }

    public ZosTSOCommandException(String message) {
        super(message);
    }

    public ZosTSOCommandException(Throwable cause) {
        super(cause);
    }

    public ZosTSOCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosTSOCommandException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
