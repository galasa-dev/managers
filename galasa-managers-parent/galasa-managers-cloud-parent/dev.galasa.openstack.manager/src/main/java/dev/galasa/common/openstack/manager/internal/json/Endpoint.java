package dev.galasa.common.openstack.manager.internal.json;

import com.google.gson.annotations.SerializedName;

public class Endpoint {

	@SerializedName("interface")
	public String endpoint_interface; // NOSONAR
	public String url; // NOSONAR
	
}
