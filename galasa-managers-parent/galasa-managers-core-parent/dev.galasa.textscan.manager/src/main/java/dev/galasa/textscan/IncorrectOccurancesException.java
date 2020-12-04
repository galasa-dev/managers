/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

public class IncorrectOccurancesException extends MissingTextException {
	private static final long serialVersionUID = 1L;

	public IncorrectOccurancesException() {
	}

	public IncorrectOccurancesException(String message) {
		super(message);
	}

	public IncorrectOccurancesException(Throwable cause) {
		super(cause);
	}

	public IncorrectOccurancesException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncorrectOccurancesException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
