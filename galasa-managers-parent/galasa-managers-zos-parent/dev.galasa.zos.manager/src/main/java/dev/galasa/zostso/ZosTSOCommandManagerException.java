/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso;

import dev.galasa.zos.ZosManagerException;

public class ZosTSOCommandManagerException extends ZosManagerException {
    private static final long serialVersionUID = 1L;

    public ZosTSOCommandManagerException() {
    }

    public ZosTSOCommandManagerException(String message) {
        super(message);
    }

    public ZosTSOCommandManagerException(Throwable cause) {
        super(cause);
    }

    public ZosTSOCommandManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosTSOCommandManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
