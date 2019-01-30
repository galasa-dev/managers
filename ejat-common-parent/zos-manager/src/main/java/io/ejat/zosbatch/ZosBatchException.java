package io.ejat.zosbatch;

public class ZosBatchException extends ZosBatchManagerException {
	private static final long serialVersionUID = 1L;

	public ZosBatchException() {
	}

	public ZosBatchException(String message) {
		super(message);
	}

	public ZosBatchException(Throwable cause) {
		super(cause);
	}

	public ZosBatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosBatchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
