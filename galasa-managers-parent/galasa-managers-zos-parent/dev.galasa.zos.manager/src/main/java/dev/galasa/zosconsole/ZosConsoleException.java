/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole;

public class ZosConsoleException extends ZosConsoleManagerException {
	private static final long serialVersionUID = 1L;

	public ZosConsoleException() {
	}

	public ZosConsoleException(String message) {
		super(message);
	}

	public ZosConsoleException(Throwable cause) {
		super(cause);
	}

	public ZosConsoleException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosConsoleException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
