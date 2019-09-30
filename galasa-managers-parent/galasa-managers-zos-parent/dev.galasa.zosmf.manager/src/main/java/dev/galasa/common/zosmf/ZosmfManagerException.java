package dev.galasa.common.zosmf;

import dev.galasa.common.zos.ZosManagerException;

public class ZosmfManagerException extends ZosManagerException {
	private static final long serialVersionUID = 1L;

	public ZosmfManagerException() {
	}

	public ZosmfManagerException(String message) {
		super(message);
	}

	public ZosmfManagerException(Throwable cause) {
		super(cause);
	}

	public ZosmfManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosmfManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
