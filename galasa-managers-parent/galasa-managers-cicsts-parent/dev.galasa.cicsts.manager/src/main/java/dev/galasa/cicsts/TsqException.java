/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

/*
 * TsqException happens for errors in the TSQ manager methods. 
 */
public class TsqException extends TsqManagerException {
    private static final long serialVersionUID = 1L;

    public TsqException() {
    }

    public TsqException(String message) {
        super(message);
    }

    public TsqException(Throwable cause) {
        super(cause);
    }

    public TsqException(String message, Throwable cause) {
        super(message, cause);
    }

    public TsqException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
