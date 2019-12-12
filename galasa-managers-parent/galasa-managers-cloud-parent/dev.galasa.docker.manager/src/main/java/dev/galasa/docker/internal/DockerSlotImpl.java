package dev.galasa.docker.internal;

import java.util.HashMap;
import java.util.Set;

/**
 * Docker slot implemtnation to limit the number of concurrently running docker containers.
 * 
 * @author James Davies
 */
public class DockerSlotImpl {

	private final DockerManagerImpl dockerManager;
	private final HashMap<String,String> resourceProperties;
	private final String slotName;

	/**
	 * Slot stores all the resource properties to aid for clean up.
	 * 
	 * @param dockerManager
	 * @param slotName
	 * @param resourceProperties
	 */
	public DockerSlotImpl(DockerManagerImpl dockerManager, String slotName, HashMap<String,String> resourceProperties) {
		this.dockerManager = dockerManager;
		this.slotName = slotName;
		this.resourceProperties = resourceProperties;
    }

	/**
	 * Free the docker slot with the manager.
	 * 
	 * @throws Exception
	 */
	public void free() throws Exception {
		dockerManager.freeDockerSlot(this);
	}

	/**
	 * Return slot name. E.g. SLOT_L1_0
	 * 
	 * @return String
	 */
	public String getSlotName() {
		return this.slotName;
	}

	/**
	 * Return a set of the resource properties used by this slot.
	 * 
	 * @return Set<String> all the keys
	 */
	public Set<String> getResourcePropertyKeys() {
		return this.resourceProperties.keySet();
	}
}