package io.ejat.zosbatch;

import io.ejat.zos.ZosManagerException;

public class ZosBatchManagerException extends ZosManagerException {
	private static final long serialVersionUID = 1L;

	public ZosBatchManagerException() {
	}

	public ZosBatchManagerException(String message) {
		super(message);
	}

	public ZosBatchManagerException(Throwable cause) {
		super(cause);
	}

	public ZosBatchManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosBatchManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
