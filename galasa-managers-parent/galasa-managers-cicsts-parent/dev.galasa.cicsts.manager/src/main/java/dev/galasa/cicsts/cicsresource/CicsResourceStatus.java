/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.cicsts.cicsresource;

public enum CicsResourceStatus {
	ENABLED("Enabled"),
	DISABLED("Disabled");
	
	private final String status;
	
	CicsResourceStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return this.status;
	}
}