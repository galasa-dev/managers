package dev.galasa.elastic;

import dev.galasa.ManagerException;

public class ElasticManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public ElasticManagerException() {
	}

	public ElasticManagerException(String message) {
		super(message);
	}

	public ElasticManagerException(Throwable cause) {
		super(cause);
	}

	public ElasticManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ElasticManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}