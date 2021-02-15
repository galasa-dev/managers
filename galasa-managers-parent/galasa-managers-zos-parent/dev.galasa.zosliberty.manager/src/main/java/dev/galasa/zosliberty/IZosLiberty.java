/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

/**
 * TODO
 */
public interface IZosLiberty {
	
	/**
	 * Create a zOS Liberty server object using the Liberty/Galasa default properties
	 * @return the Galasa Liberty server object
	 */
	public IZosLibertyServer newZosLibertyServer() throws ZosLibertyServerException;
	
	//TODO: more methods to create using supplied parameters
}
