/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.cicsts.cicsresource;

public class CicsJvmserverResourceException extends CicsResourceManagerException {
    private static final long serialVersionUID = 1L;

    public CicsJvmserverResourceException() {
    }

    public CicsJvmserverResourceException(String message) {
        super(message);
    }

    public CicsJvmserverResourceException(Throwable cause) {
        super(cause);
    }

    public CicsJvmserverResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CicsJvmserverResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
