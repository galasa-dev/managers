package dev.voras.common.linux.internal;

import dev.voras.common.ipnetwork.IIpHost;

public class LinuxDSEIpHost implements IIpHost {
	
	private final String hostname;
	
	protected LinuxDSEIpHost(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public String getHostname() {
		return this.hostname;
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
