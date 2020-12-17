/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

public class FailTextFoundException extends TextScanManagerException {
	private static final long serialVersionUID = 1L;

	public FailTextFoundException() {
	}

	public FailTextFoundException(String message) {
		super(message);
	}

	public FailTextFoundException(Throwable cause) {
		super(cause);
	}

	public FailTextFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailTextFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
