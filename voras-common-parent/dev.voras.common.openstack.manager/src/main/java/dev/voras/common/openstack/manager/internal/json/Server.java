package dev.voras.common.openstack.manager.internal.json;

import com.google.gson.annotations.SerializedName;

public class Server {

	public String  id;
	public String  name;
	public String  imageRef;
	public String  flavorRef;
	public String  networks;
	public String  availability_zone;
	public String  adminPass;
	public String  key_name;
	
	@SerializedName("OS-EXT-STS:power_state")
	public Integer power_state;
	
	@SerializedName("OS-EXT-STS:task_state")
	public String task_state;
	
	public VorasMetadata metadata;
	
}
