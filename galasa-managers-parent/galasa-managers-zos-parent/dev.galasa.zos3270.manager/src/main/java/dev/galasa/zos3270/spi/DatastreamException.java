/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.spi;

public class DatastreamException extends NetworkException {
    private static final long serialVersionUID = 1L;

    public DatastreamException() {
    }

    public DatastreamException(String message) {
        super(message);
    }

    public DatastreamException(Throwable cause) {
        super(cause);
    }

    public DatastreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatastreamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
