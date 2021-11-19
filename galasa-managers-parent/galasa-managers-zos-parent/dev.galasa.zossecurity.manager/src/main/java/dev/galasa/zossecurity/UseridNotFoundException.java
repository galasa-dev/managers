/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zossecurity;

public class UseridNotFoundException extends ZosSecurityManagerException {
	private static final long serialVersionUID = 1L;

	public UseridNotFoundException() {
	}

	public UseridNotFoundException(String message) {
		super(message);
	}

	public UseridNotFoundException(Throwable cause) {
		super(cause);
	}

	public UseridNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
