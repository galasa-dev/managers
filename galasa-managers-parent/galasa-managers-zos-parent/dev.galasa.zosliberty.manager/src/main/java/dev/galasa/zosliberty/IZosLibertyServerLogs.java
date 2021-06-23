/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

import java.io.OutputStream;

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
	public OutputStream getLog(String fileName);

	/**
	 * Get the Liberty messages.log
	 * @return the log file
	 */
	public OutputStream getMessagesLog();

	/**
	 * Get the next FFDC log file
	 * @return the next FFDC log 
	 */
	public OutputStream getNextFfdc();

	/**
	 * Get the next log file
	 * @return the next log file
	 */
	public OutputStream getNext();

	/**
	 * The the name of the current log file
	 * @return the log file name
	 */
	public String getCurrentLogName();

	public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException;
}
