/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle.windows;

import dev.galasa.gradle.GradleManagerException;

/**
 * Used when throwing an exception specific to the Gradle Windows Manager.
 * 
 * @author Matthew Chivers
 * 
 */
public class GradleWindowsManagerException extends GradleManagerException {
    private static final long serialVersionUID = 1L;

    public GradleWindowsManagerException() {
    }

    public GradleWindowsManagerException(String message) {
        super(message);
    }

    public GradleWindowsManagerException(Throwable cause) {
        super(cause);
    }

    public GradleWindowsManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GradleWindowsManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
