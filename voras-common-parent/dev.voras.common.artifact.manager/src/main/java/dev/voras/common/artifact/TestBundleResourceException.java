package dev.voras.common.artifact;

import dev.voras.ManagerException;

public class TestBundleResourceException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public TestBundleResourceException() {
	}

	public TestBundleResourceException(String message) {
		super(message);
	}

	public TestBundleResourceException(Throwable throwable) {
		super(throwable);
	}

	public TestBundleResourceException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
