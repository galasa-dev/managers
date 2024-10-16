/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

/*
 * TsqManagerException happens for errors in TSQ manager provisioning
 */
public class TsqManagerException extends CicstsManagerException {
    private static final long serialVersionUID = 1L;

    public TsqManagerException() {
    }

    public TsqManagerException(String message) {
        super(message);
    }

    public TsqManagerException(Throwable cause) {
        super(cause);
    }

    public TsqManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TsqManagerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
