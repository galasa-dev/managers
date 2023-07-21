/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem;

import dev.galasa.cicsts.CicstsManagerException;

public class SemManagerException extends CicstsManagerException {
    private static final long serialVersionUID = 1L;

    public SemManagerException() {
    }

    public SemManagerException(String message) {
        super(message);
    }

    public SemManagerException(Throwable cause) {
        super(cause);
    }

    public SemManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SemManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
