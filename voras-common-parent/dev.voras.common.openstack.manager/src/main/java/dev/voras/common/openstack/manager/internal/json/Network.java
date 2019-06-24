package dev.voras.common.openstack.manager.internal.json;

import com.google.gson.annotations.SerializedName;

public class Network {
	
	public String id;
	public String name;
	
	@SerializedName("router:external")
	public boolean route_external;

}
