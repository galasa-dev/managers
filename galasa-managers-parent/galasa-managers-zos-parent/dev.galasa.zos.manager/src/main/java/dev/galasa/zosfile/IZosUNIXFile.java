package dev.galasa.zosfile;

import java.util.Map;

import dev.galasa.zosfile.IZosDataset.DatasetDataType;

/**
 * Representation of a UNIX file or directory.
 *
 */
public interface IZosUNIXFile {
	
	/**
	 * Enumeration of data type for store and retrieve of data set content:
	 * <li>{@link #TEXT}</li>
	 * <li>{@link #BINARY}</li>
	 */
	public enum UNIXFileDataType {
		/**
		 * Content is between ISO8859-1 on the client and EBCDIC on the host
		 */
		TEXT("text"),
		/**
		 * No data conversion is performed
		 */
		BINARY("binary");
		
		private String dataType;
		
		UNIXFileDataType(String dataType) {
			this.dataType = dataType;
		}
		
		@Override
		public String toString() {
			return dataType;
		}
	}
	
	/**
	 * Create the zOS UNIX file or directory. Will be deleted at test method end
	 * @return
	 * @throws ZoException 
	 */
	public IZosUNIXFile create() throws ZosUNIXFileException;

	/**
	 * Create the zOS UNIX file or directory from the zOS image. Will be retained across test methods and deleted at test class end
	 * @return
	 * @throws ZoException 
	 */
	public IZosUNIXFile createRetain() throws ZosUNIXFileException;

	/**
	 * Delete the zOS UNIX file or directory from the zOS image. Attempting to delete a non-empty directory Will 
	 * @throws ZosUNIXFileException
	 */
	public void delete() throws ZosUNIXFileException;
	
	/**
	 * Recursively delete the zOS UNIX directory and its contents from the zOS image
	 * @throws ZosUNIXFileException
	 */
	public void directoryDeleteNonEmpty() throws ZosUNIXFileException;

	/**
	 * Return true if the zOS UNIX exists on the zOS image
	 * @return
	 * @throws ZosUNIXFileException
	 */
	public boolean exists() throws ZosUNIXFileException;

	/**
	 * Write the content to the zOS UNIX file on the zOS image. Data type is can be set by {@link #setDataType(UNIXFileDataType)}
	 */
	public void store(String content) throws ZosUNIXFileException;

	/**
	 * Retrieve the content of the zOS UNIX file from the zOS image. Data type is can be set by {@link #setDataType(UNIXFileDataType)}
	 * @throws ZosUNIXFileException
	 */
	public String retrieve() throws ZosUNIXFileException;

	/**
	 * Recursively store the content of the zOS UNIX file or directory to the test output 
	 * @throws ZosUNIXFileException
	 */
	public void saveToResultsArchive() throws ZosUNIXFileException;
	
	/**
	 * Return true if this object represents a zOS UNIX directory
	 * @return
	 * @throws ZosUNIXFileException
	 */
	public boolean isDirectory() throws ZosUNIXFileException;
	
	/**
	 * Returns sorted {@link Map} the zOS UNIX files and directories in this zOS UNIX directory
	 * @return
	 * @throws ZosUNIXFileException
	 */
	public Map<String, String> directoryList() throws ZosUNIXFileException;
	
	/**
	 * Returns recursive sorted {@link Map} the zOS UNIX files and directories in this zOS UNIX directory
	 * @return
	 * @throws ZosUNIXFileException
	 */
	public Map<String, String> directoryListRecursive() throws ZosUNIXFileException;
	
	/**
	 * Set the data type ({@link UNIXFileDataType}) for store and retrieve of the zOS UNIX file content
	 * @param dataType
	 */
	public void setDataType(UNIXFileDataType dataType);
	
	/**
	 * Return the data type ({@link UNIXFileDataType}) for store and retrieve of the zOS UNIX file content
	 * @param dataType
	 */
	public UNIXFileDataType getDataType();
	
	/**
	 * Return the path of the zOS UNIX file or directory
	 * @param dataType
	 */
	public String getUnixPath();
	
	/**
	 * Return the file name of the zOS UNIX file, or null if this object represents a directory 
	 * @return
	 */
	public String getFileName();

	/**
	 * Get the directory path for the zOS UNIX file or directory
	 * @return
	 */
	public String getDirectoryPath();

	/**
	 * Return the attributes of the zOS UNIX file or directory as a {@link String} 
	 * @return
	 * @throws ZosUNIXFileException
	 */
	public String getAttributesAsString() throws ZosUNIXFileException;
}
