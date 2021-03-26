/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
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