package dev.voras.common.zos;

import dev.voras.framework.spi.ManagerException;

public class ZosManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public ZosManagerException() {
	}

	public ZosManagerException(String message) {
		super(message);
	}

	public ZosManagerException(Throwable cause) {
		super(cause);
	}

	public ZosManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
