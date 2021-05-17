/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

public class TextScanException extends TextScanManagerException {
	private static final long serialVersionUID = 1L;

	public TextScanException() {
	}

	public TextScanException(String message) {
		super(message);
	}

	public TextScanException(Throwable cause) {
		super(cause);
	}

	public TextScanException(String message, Throwable cause) {
		super(message, cause);
	}

	public TextScanException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
