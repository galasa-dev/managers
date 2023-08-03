/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens.images;

public class TerminalImageException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public TerminalImageException() {
    }

    public TerminalImageException(String message) {
        super(message);
    }

    public TerminalImageException(Throwable cause) {
        super(cause);
    }

    public TerminalImageException(String message, Throwable cause) {
        super(message, cause);
    }

    public TerminalImageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}