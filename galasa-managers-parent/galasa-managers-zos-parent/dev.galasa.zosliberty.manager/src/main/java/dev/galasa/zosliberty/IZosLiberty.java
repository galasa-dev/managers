/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosUNIXFile;

/**
 * TODO
 */
public interface IZosLiberty {
	
	/**
	 * Create a zOS Liberty server object using the Liberty/Galasa default properties
	 * @return the zOS Liberty server object
	 */
	public IZosLibertyServer newZosLibertyServer() throws ZosLibertyServerException;
	
	/**
	 * Create a zOS Liberty server object using the Liberty/Galasa default properties
	 * @param zosImage the zOS image for this Liberty server
	 * @param wlpInstallDir the Liberty install directory ($WLP_INSTALL_DIR)
	 * @param wlpUserDir the Liberty user directory ($WLP_USER_DIR)
	 * @param wlpUserDir the Liberty output directory ($WLP_OUTPUT_DIR)
	 * @return the zOS Liberty server object
	 */
	public IZosLibertyServer newZosLibertyServer(IZosImage zosImage, IZosUNIXFile wlpInstallDir, IZosUNIXFile wlpUserDir, IZosUNIXFile wlpOutputDir) throws ZosLibertyServerException;
}
