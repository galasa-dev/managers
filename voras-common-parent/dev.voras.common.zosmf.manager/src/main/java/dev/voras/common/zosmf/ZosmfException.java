package dev.voras.common.zosmf;

public class ZosmfException extends ZosmfManagerException {
	private static final long serialVersionUID = 1L;

	public ZosmfException() {
	}

	public ZosmfException(String message) {
		super(message);
	}

	public ZosmfException(Throwable cause) {
		super(cause);
	}

	public ZosmfException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosmfException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
