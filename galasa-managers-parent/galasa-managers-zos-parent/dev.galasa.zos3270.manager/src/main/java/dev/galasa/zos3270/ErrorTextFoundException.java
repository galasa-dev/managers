/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270;

public class ErrorTextFoundException extends Zos3270Exception {
    private static final long serialVersionUID = 1L;
    
    private final int errorId;

    public ErrorTextFoundException(String message, int errorId) {
        super(message);
        this.errorId = errorId;
    }

    public int getErrorId() {
        return this.errorId;
    }
}
