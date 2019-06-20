package dev.voras.common.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface IBundleResources {
	
	public static final String RESOURCES_DIRECTORY = "resources";

	/**
	 * Retrieve a single file as an InputStream
	 *
	 * @param path
	 * @return
	 * @throws TestBundleResourceException
	 */
	public InputStream retrieveFile(String path) throws TestBundleResourceException;

	/**
	 * Retrieve a single file as an InputStream, if the file is a skeleton then any required substitutions
	 * will be performed before the stream is returned. 
	 * 
	 * @param path
	 * @param parameters
	 * @param skeletonType
	 * @return
	 * @throws TestBundleResourceException
	 */
	public InputStream retrieveSkeletonFile(String path, Map<String, Object> parameters, int skeletonType) throws TestBundleResourceException;


	/**
	 * Retrieve a single file as an InputStream, if the file is a skeleton then any required substitutions
	 * will be performed before the stream is returned. Uses the default skeleton processor {@link SkeletonType#PLUSPLUS}
	 * 
	 * @param path
	 * @param parameters
	 * @return
	 * @throws TestBundleResourceException 
	 */
	public InputStream retrieveSkeletonFile(String path, Map<String, Object> parameters) throws TestBundleResourceException;
	
	/**
	 * For a directory, retrieve a map of file paths within it, and InputStreams for those files. 
	 * 
	 * @param directory
	 * @return
	 * @throws TestBundleResourceException
	 */
	public Map<String, InputStream> retrieveDirectoryContents(String directory) throws TestBundleResourceException;
	
	/**
	 * For a directory, retrieve a map of file paths within it, and InputStreams for those files, if the file is a skeleton then any required substitutions
	 * will be performed before the stream is returned. 
	 * 
	 * @param directory
	 * @return
	 * @throws TestBundleResourceException
	 */
	public Map<String, InputStream> retrieveSkeletonDirectoryContents(String directory, Map<String, Object> parameters, int skeletonType) throws TestBundleResourceException;
	
	/**
	 * Retrieve a jar file as an input stream
	 * 
	 * @param symbolicName
	 * @param version
	 * @param directory
	 * @return
	 * @throws TestBundleResourceException
	 */
	public InputStream retrieveJar(String symbolicName, String version, String directory) throws TestBundleResourceException;
	
	/**
	 * Convert the contents of a directory (as returned by {@link #retrieveDirectoryContents(String)} or {@link #retrieveSkeletonDirectoryContents(String, HashMap)}
	 * into a zip input stream for quicker deployment.
	 * 
	 * @param directoryContents
	 * @return
	 * @throws TestBundleResourceException
	 */
	public InputStream tarDirectoryContents(String resourcesDirectory, Map<String, InputStream> directoryContents, String encoding, boolean gzip) throws TestBundleResourceException;
	
	/**
	 * Convert an input stream into a list of strings
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public List<String> streamAsList(InputStream file) throws IOException;

	/**
	 * Convert an input stream into a string
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String streamAsString(InputStream file) throws IOException;

	public InputStream zipDirectoryContents(String resourcesDirectory, Map<String, Object> parameters, String encoding,
			boolean gzip) throws TestBundleResourceException;
	
}
