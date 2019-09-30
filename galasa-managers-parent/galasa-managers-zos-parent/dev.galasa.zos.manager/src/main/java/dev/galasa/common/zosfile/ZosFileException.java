package dev.galasa.common.zosfile;

public class ZosFileException extends ZosFileManagerException {
	private static final long serialVersionUID = 1L;

	public ZosFileException() {
	}

	public ZosFileException(String message) {
		super(message);
	}

	public ZosFileException(Throwable cause) {
		super(cause);
	}

	public ZosFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZosFileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
