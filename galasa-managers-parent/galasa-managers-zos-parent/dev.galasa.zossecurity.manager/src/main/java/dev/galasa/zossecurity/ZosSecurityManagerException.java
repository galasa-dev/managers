/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zossecurity;

import dev.galasa.zos.ZosManagerException;

public class ZosSecurityManagerException extends ZosManagerException {
	private static final long serialVersionUID = 1L;

	public ZosSecurityManagerException() {
	}

	public ZosSecurityManagerException(String message) {
		super(message);
	}

	public ZosSecurityManagerException(Throwable cause) {
		super(cause);
	}

	public ZosSecurityManagerException(String message, Throwable cause) {
		super(message, cause);
	}

}
