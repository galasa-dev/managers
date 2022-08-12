/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.artifact;

import dev.galasa.ManagerException;

public class SkeletonProcessorException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public SkeletonProcessorException() {
    }

    public SkeletonProcessorException(String message) {
        super(message);
    }

    public SkeletonProcessorException(Throwable throwable) {
        super(throwable);
    }

    public SkeletonProcessorException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
