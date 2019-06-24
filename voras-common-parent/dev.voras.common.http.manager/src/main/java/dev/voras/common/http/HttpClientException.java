package dev.voras.common.http;

import dev.voras.ManagerException;

public class HttpClientException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public HttpClientException() {
	}

	public HttpClientException(String message) {
		super(message);
	}

	public HttpClientException(Throwable cause) {
		super(cause);
	}

	public HttpClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
