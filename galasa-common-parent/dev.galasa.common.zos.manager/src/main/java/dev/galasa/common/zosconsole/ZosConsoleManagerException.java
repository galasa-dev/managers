package dev.galasa.common.zosconsole;

import dev.galasa.common.zos.ZosManagerException;

public class ZosConsoleManagerException extends ZosManagerException {
	private static final long serialVersionUID = 1L;

	public ZosConsoleManagerException() {
	}

	public ZosConsoleManagerException(String message) {
		super(message);
	}

	public ZosConsoleManagerException(Throwable cause) {
		super(cause);
	}

	public ZosConsoleManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosConsoleManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
