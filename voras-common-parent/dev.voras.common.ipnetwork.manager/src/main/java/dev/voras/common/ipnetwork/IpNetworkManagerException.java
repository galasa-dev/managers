package dev.voras.common.ipnetwork;

import dev.voras.framework.spi.ManagerException;

public class IpNetworkManagerException extends ManagerException {
	private static final long serialVersionUID = 1L;

	public IpNetworkManagerException() {
	}

	public IpNetworkManagerException(String message) {
		super(message);
	}

	public IpNetworkManagerException(Throwable cause) {
		super(cause);
	}

	public IpNetworkManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public IpNetworkManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
