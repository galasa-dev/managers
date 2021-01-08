/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunixcommand;

public class ZosUNIXCommandAuthFailException extends ZosUNIXCommandException {
    private static final long serialVersionUID = 1L;

    public ZosUNIXCommandAuthFailException() {
    }

    public ZosUNIXCommandAuthFailException(String message) {
        super(message);
    }

    public ZosUNIXCommandAuthFailException(Throwable cause) {
        super(cause);
    }

    public ZosUNIXCommandAuthFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosUNIXCommandAuthFailException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
