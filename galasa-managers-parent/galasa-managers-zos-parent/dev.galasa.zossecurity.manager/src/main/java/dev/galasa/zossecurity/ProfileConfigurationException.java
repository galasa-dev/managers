/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zossecurity;

public class ProfileConfigurationException extends ZosSecurityManagerException {
	private static final long serialVersionUID = 1L;

	public ProfileConfigurationException() {
	}

	public ProfileConfigurationException(String message) {
		super(message);
	}

	public ProfileConfigurationException(Throwable cause) {
		super(cause);
	}

	public ProfileConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
