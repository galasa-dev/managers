package dev.galasa.zosfile;

import java.util.Map;

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
	
	public IZosUNIXFile create() throws ZosUNIXFileException;

	IZosUNIXFile createRetain() throws ZosUNIXFileException;

	/**
	 * Delete the UNIX file or directory corresponding to an {@link IZosUNIXFile} from the given zOS image.
	 * Recursively delete a directory and its contents from the given zOS image
	 * @throws ZosUNIXFileException
	 */
	public void delete() throws ZosUNIXFileException;
	
	public void directoryDeleteNonEmpty() throws ZosUNIXFileException;

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
	public void store(String content) throws ZosUNIXFileException;

	/**
	 * Retrieve the content of UNIX file to an {@link IZosUNIXFile} from the given zOS
	 * image. The content can then be obtained using the
	 * {@link IZosUNIXFile#getContent()} method.
	 * @throws ZosUNIXFileException
	 */
	public String retrieve() throws ZosUNIXFileException;

	public void saveToResultsArchive() throws ZosUNIXFileException;
	
	public boolean isDirectory() throws ZosUNIXFileException;
	
	public Map<String, String> directoryList() throws ZosUNIXFileException;
	
	public Map<String, String> directoryListRecursive() throws ZosUNIXFileException;
	
	public void setDataType(UNIXFileDataType dataType);
	
	public UNIXFileDataType getDataType();
	
	public String getUnixPath();
	
	/**
	 * Get the file name, or null if this object
	 * represents a directory 
	 * @return
	 */
	public String getFileName();

	/**
	 * Get the directory path for this file 
	 * @return
	 */
	public String getDirectoryPath();

	public String getAttributesAsString() throws ZosUNIXFileException;
}
