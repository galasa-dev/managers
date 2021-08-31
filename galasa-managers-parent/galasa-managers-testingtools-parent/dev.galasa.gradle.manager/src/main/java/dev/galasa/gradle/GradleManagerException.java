/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle;

import dev.galasa.ManagerException;

/**
 * Used when throwing an exception specific to the Gradle Manager.
 * 
 * @author Matthew Chivers
 * 
 */
public class GradleManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public GradleManagerException() {
    }

    public GradleManagerException(String message) {
        super(message);
    }

    public GradleManagerException(Throwable cause) {
        super(cause);
    }

    public GradleManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GradleManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
