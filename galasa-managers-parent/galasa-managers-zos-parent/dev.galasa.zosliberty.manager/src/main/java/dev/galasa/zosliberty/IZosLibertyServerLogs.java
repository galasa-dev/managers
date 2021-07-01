/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

import dev.galasa.zosfile.IZosUNIXFile;

/**
 * An IZosLibertyServerLogs contain an array of Liberty server logs
 */
public interface IZosLibertyServerLogs {
	/**
	 * Does this Liberty server have any FFDC logs
	 * @return true or false
	 */
	public boolean hasFFDC();

	/**
	 * The number of Liberty log files
	 * @return the number
	 */
	public int numberOfFiles();

	/**
	 * Get a log file by name
	 * @param fileName the log file name
	 * @return the log file
	 */
	public IZosUNIXFile getLog(String fileName);

	/**
	 * Get the next log file
	 * @return the next log file
	 */
	public IZosUNIXFile getNext();

	/**
	 * Get the Liberty messages.log
	 * @return the log file
	 */
	public IZosUNIXFile getMessagesLog();

	/**
	 * Get the next FFDC log file
	 * @return the next FFDC log 
	 */
	public IZosUNIXFile getNextFfdc();

	/**
	 * The the name of the current log file
	 * @return the log file name
	 */
	public String getCurrentLogName();

	/**
	 * Store the content of the Liberty server logs to the Results Archive Store
	 * @param rasPath path in Results Archive
	 * @throws ZosLibertyServerException
	 */
	public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException;
}
