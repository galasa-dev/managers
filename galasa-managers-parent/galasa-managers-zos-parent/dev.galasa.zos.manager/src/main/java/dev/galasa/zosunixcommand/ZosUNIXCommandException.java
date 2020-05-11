/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunixcommand;

public class ZosUNIXCommandException extends ZosUNIXCommandManagerException {
    private static final long serialVersionUID = 1L;

    public ZosUNIXCommandException() {
    }

    public ZosUNIXCommandException(String message) {
        super(message);
    }

    public ZosUNIXCommandException(Throwable cause) {
        super(cause);
    }

    public ZosUNIXCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosUNIXCommandException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
