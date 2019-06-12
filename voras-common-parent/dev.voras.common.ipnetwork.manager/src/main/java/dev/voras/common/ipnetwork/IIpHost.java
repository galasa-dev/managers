package dev.voras.common.ipnetwork;

/**
 * <p>Represents a IP Host or Stack.</p>
 * 
 * <p>Use the appropriate host manager annotation to obtain an object</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IIpHost {

	/**
	 * Get the default Hostname of the Host
	 */
	String getHostname();
	
	
	/**
	 * Does this IP Host have enough information to make it valid
	 * 
	 * @return true if valid
	 */
	boolean isValid();
}
