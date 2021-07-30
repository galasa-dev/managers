/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

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
    public IZosLibertyServerLog getLog(String fileName);

    /**
     * Get the next log file
     * @return the next log file
     */
    public IZosLibertyServerLog getNext();

    /**
     * Get the Liberty messages.log
     * @return the log file
     */
    public IZosLibertyServerLog getMessagesLog();

    /**
     * Get the next FFDC log file
     * @return the next FFDC log 
     */
    public IZosLibertyServerLog getNextFfdc();

    /**
     * The the name of the current log file
     * @return the log file name
     */
    public String getCurrentLogName();

    /**
     * Refresh the list of log files
     * @throws ZosLibertyServerException
     */
    public void refresh() throws ZosLibertyServerException;

	/**
     * Store the content of the Liberty server logs to the Results Archive Store
     * @param rasPath path in Results Archive
     * @throws ZosLibertyServerException
     */
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException;
}
