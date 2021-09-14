/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle.ubuntu;

import dev.galasa.gradle.GradleManagerException;

/**
 * Used when throwing an exception specific to the Gradle Ubuntu Manager.
 * 
 * @author Matthew Chivers
 * 
 */
public class GradleUbuntuManagerException extends GradleManagerException {
    private static final long serialVersionUID = 1L;

    public GradleUbuntuManagerException() {
    }

    public GradleUbuntuManagerException(String message) {
        super(message);
    }

    public GradleUbuntuManagerException(Throwable cause) {
        super(cause);
    }

    public GradleUbuntuManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GradleUbuntuManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
