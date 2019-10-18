package dev.galasa.zosfile;

/**
 * Representation of a UNIX file or directory.
 *
 */
public interface IZosUNIXFile {

	/**
	 * Create a directory and all required parent directories on the given zOS image.
	 * @throws ZosUNIXFileException
	 */
	public void createDirectory(String directory) throws ZosUNIXFileException;

	/**
	 * Recursively delete a directory and its contents from the given zOS image.
	 * @throws ZosUNIXFileException
	 */
	public void deleteDirectory(String directory) throws ZosUNIXFileException;

	/**
	 * Return true if the passed {@link IZosUNIXFile} exists on the given zOS image.
	 * @return
	 * @throws ZosUNIXFileException
	 */
	public boolean exists() throws ZosUNIXFileException;

	/**
	 * Store an {@link IZosUNIXFile} as a file on the given zOS image, creating the file
	 * and all required parent directories as required. The content stored is
	 * that added to the unixFile using the {@link IZosUNIXFile#setContent(String)}
	 * or {@link IZosUNIXFile#appendContent(String)} methods.
	 * @throws ZosUNIXFileException
	 */
	public void store() throws ZosUNIXFileException;

	/**
	 * Retrieve the content of UNIX file to an {@link IZosUNIXFile} from the given zOS
	 * image. The content can then be obtained using the
	 * {@link IZosUNIXFile#getContent()} method.
	 * @throws ZosUNIXFileException
	 */
	public void retrieve() throws ZosUNIXFileException;

	/**
	 * Get the directory path for this file 
	 * @return
	 */
	public String getDirectory();
	
	/**
	 * Get the file name, or null if this object
	 * represents a directory 
	 * @return
	 */
	public String getFileName();


	/**
	 * Delete the UNIX file corresponding to an {@link IZosUNIXFile} from the given zOS image.
	 * @throws ZosUNIXFileException
	 */
	public void delete() throws ZosUNIXFileException;
}
