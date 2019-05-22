package io.ejat.ipnetwork;

/**
 * <p>Represents a IP Port.</p>
 * 
 * <p>Use the appropriate host manager annotation to obtain an object</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IIpPort {

	/**
	 * Get the port number
	 */
	int getPortNumber();

	IIpHost getHost();

	String getType();
}
