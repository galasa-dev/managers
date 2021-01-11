/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2020.
 */
package dev.galasa.ipnetwork;

/**
 * zOS Command exception indicating an error in SSH
 * 
 * @author James Bartlett
 *
 */
public class SSHException extends IpNetworkManagerException {
    private static final long serialVersionUID = 1L;

    public SSHException() {
    }

    public SSHException(String message) {
        super(message);
    }

    public SSHException(Throwable throwable) {
        super(throwable);
    }

    public SSHException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
