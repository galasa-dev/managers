/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.jmeter.internal;

import dev.galasa.ManagerException;

public class JMeterManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public JMeterManagerException() {
	}

	public JMeterManagerException(String message) {
		super(message);
	}

	public JMeterManagerException(Throwable cause) {
		super(cause);
	}

	public JMeterManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public JMeterManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}