/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.ipnetwork;

public class SSHAuthFailException extends SSHException {
    private static final long serialVersionUID = 1L;

    public SSHAuthFailException() {
    }

    public SSHAuthFailException(String message) {
        super(message);
    }

    public SSHAuthFailException(Throwable throwable) {
        super(throwable);
    }

    public SSHAuthFailException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
