/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile;

public class ZosUNIXFileException extends ZosFileManagerException {
	private static final long serialVersionUID = 1L;

	public ZosUNIXFileException() {
	}

	public ZosUNIXFileException(String message) {
		super(message);
	}

	public ZosUNIXFileException(Throwable cause) {
		super(cause);
	}

	public ZosUNIXFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosUNIXFileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
