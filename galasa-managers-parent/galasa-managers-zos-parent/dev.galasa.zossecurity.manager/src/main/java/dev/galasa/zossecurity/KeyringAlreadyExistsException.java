/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zossecurity;

public class KeyringAlreadyExistsException extends ZosSecurityManagerException {
	private static final long serialVersionUID = 1L;

	public KeyringAlreadyExistsException() {
	}

	public KeyringAlreadyExistsException(String message) {
		super(message);
	}

	public KeyringAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public KeyringAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}
