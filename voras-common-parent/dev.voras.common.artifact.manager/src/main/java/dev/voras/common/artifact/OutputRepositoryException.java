package dev.voras.common.artifact;

import dev.voras.ManagerException;

public class OutputRepositoryException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public OutputRepositoryException() {
	}

	public OutputRepositoryException(String message) {
		super(message);
	}

	public OutputRepositoryException(Throwable throwable) {
		super(throwable);
	}

	public OutputRepositoryException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
