/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

import dev.galasa.ManagerException;

public class TextScanManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public TextScanManagerException() {
	}

	public TextScanManagerException(String message) {
		super(message);
	}

	public TextScanManagerException(Throwable cause) {
		super(cause);
	}

	public TextScanManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public TextScanManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
