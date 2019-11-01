/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.openstack.manager.internal.json;

import java.util.List;

public class Port {
	
	public String id; // NOSONAR
	public String device_id; // NOSONAR
	
	public List<DnsAssignment> dns_assignment; // NOSONAR

}
