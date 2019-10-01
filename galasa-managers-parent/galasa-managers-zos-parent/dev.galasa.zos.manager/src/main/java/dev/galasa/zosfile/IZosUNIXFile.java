package dev.galasa.zosfile;

/**
 * Representation of a UNIX file or directory.
 * 
 * @author James Bartlett
 *
 */
public interface IZosUNIXFile {

	/**
	 * Get the directory path for this file
	 * 
	 * @return
	 */
	public String getDirectory();
	
	/**
	 * Get the file name, or null if this object
	 * represents a directory
	 * 
	 * @return
	 */
	public String getFileName();
}
