/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

import java.io.OutputStream;
import java.util.regex.Pattern;

import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosliberty.IZosLibertyServer;

/**
 * Represents a CICS JVM server resource. In addition to methods provided by {@link ICicsResourceBase}, provides methods to
 * set JVM server specific attributes on the resource (via CEDA) and to manage and set attributes in CEMT. Methods are 
 * provided to manage the logs files associated with the JVM server.<p>
 * JVM profile options should be managed via the {@link IJvmprofile} JVM profile object
 */
public interface IJvmserver extends ICicsResourceBase {
	
	public enum JvmserverType {
		CLASSPATH("Classpath"),
		OSGI("OSGi"),
		LIBERTY("Liberty");

		String type;
		
		JvmserverType(String type) {
			this.type = type;
		}
		
		@Override
		public String toString() {
			return this.type;
		}
	}
	
	public enum PurgeType {
		PHASEOUT,
		PURGE,
		FORCEPURGE,
		KILL
	}
	
	/**
	 * Set the CICS JVMSERVER resource LERUNOPTS attribute value
	 * @param lerunopts the resource LERUNOPTS attribute value
	 */
	public void setResourceLerunopts(String lerunopts);
	
	/**
	 * Set the CICS JVMSERVER resource THREADLIMIT attribute value
	 * @param threadlimit the resource THREADLIMIT attribute value
	 */
	public void setResourceThreadlimit(int threadlimit);
	
	/**
	 * Return the CICS JVMSERVER resource STATUS attribute value
	 * @return the resource STATUS attribute value
	 */
	public CicsResourceStatus getResourceStatus();
	
	/**
	 * Return the CICS JVMSERVER resource JVMPROFILE attribute value
	 * @return the resource JVMPROFILE attribute value
	 */
	public String getResourceJvmprofile();
	
	/**
	 * Return the CICS JVMSERVER resource LERUNOPTS attribute value
	 * @return the resource LERUNOPTS attribute value
	 */
	public String getResourceLerunopts();
	
	/**
	 * Return the CICS JVMSERVER resource THREADLIMIT attribute value
	 * @return the resource THREADLIMIT attribute value
	 */
	public int getResourceThreadlimit();	
	
	/**
	 * Set the JVMSERVER Threadlimit value in CEMT
	 * @param the Threadlimit value
	 * @throws CicsJvmserverResourceException 
	 */
	public void setThreadLimit(int threadlimit) throws CicsJvmserverResourceException;
	
	/**
	 * Get the JVMSERVER Threadlimit value from CEMT
	 * @return the Threadlimit value
	 * @throws CicsJvmserverResourceException 
	 */
	public int getThreadLimit() throws CicsJvmserverResourceException;	
	
	/**
	 * Get the JVMSERVER Threadcount value form CEMT, i.e the number of threads in use
	 * @return the Threadcount value
	 * @throws CicsJvmserverResourceException 
	 */
	public int getThreadCount() throws CicsJvmserverResourceException;

	/**
	 * Build the complete JVM server including the profile zOS UNIX file and the CICS resource definition. This method will install the CICS 
	 * resource and wait for it to become enabled
	 * @throws CicsJvmserverResourceException
	 */
	public void build() throws CicsJvmserverResourceException;
	
	/**
	 * Return the JVM profile for this JVM server
	 * @return the JVM profile object
	 */
	public IJvmprofile getJvmprofile();
	
	/**
	 * Build the JVM server profile zOS UNIX file only 
	 * @throws CicsJvmserverResourceException
	 */
	public void buildProfile() throws CicsJvmserverResourceException;	
	
	/**
	 * Checks if the JVM profile has been built
	 * @return Boolean
	 */
	public boolean isProfileBuilt();	
	
	/**
	 * Disable the CICS JVMSERVER resource with a specific {@link PurgeType} and specified timeout
	 * @param purgeType
	 * @param millisecondTimeout
	 * @return true if the resource disables within the time, false otherwise
	 * @throws CicsJvmserverResourceException
	 */
	public boolean disable(PurgeType purgeType, int millisecondTimeout) throws CicsJvmserverResourceException;
	
	/**
	 * Disable the CICS JVMSERVER resource. This method will escalate through all the {@link PurgeType} levels (PHASEOUT, PURGE, FORCEPURGE, KILL) as
	 * necessary. If the disable at any level is not successful within the stepMillisecondTimeout, escalation will happen
	 * @param stepMillisecondTimeout time to allow each step to disable before escalating
	 * @return the {@link PurgeType} at which the disable was successful
	 * @throws CicsJvmserverResourceException 
	 */
	public PurgeType disableWithEscalate(int stepMillisecondTimeout) throws CicsJvmserverResourceException;

	/**
	 * Delete the CICS JVMSERVER resource including it's zOS UNIX files and directories. If the resource is installed, it will be disabled and discarded
	 * @throws CicsJvmserverResourceException
	 */
	@Override
	public void delete() throws CicsJvmserverResourceException;	
	
	/**
	 * Delete the CICS JVMSERVER resource including it's zOS UNIX files and directories. If the resource is installed, it will be disabled and discarded. 
	 * Errors during the process will cause an exception to be thrown depending on the value of ignoreErrors 
	 * @param ignoreErrors 
	 * @throws CicsJvmserverResourceException
	 */
	public void delete(boolean ignoreErrors) throws CicsJvmserverResourceException;
	
	/**
	 * Sets the zOS Liberty server object associated with this JVM server
	 * @param zosLibertyServer the {@link IZosLibertyServer} object to associate with this JVM server
	 */
	public void setLibertyServer(IZosLibertyServer zosLibertyServer) throws CicsJvmserverResourceException;

	/**
	 * Returns the zOS Liberty server object associated with this JVM server
	 * @return the {@link IZosLibertyServer} associated with this JVM server
	 */
	public IZosLibertyServer getLibertyServer();
	
	/**
	 * Convenience method that returns the JAVA_HOME as defined in the JVM Profile
	 * @return the JAVA_HOME value
	 */
	public String getJavaHome();
	
	/**
	 * Convenience method that returns the WORK_DIR as defined in the JVM Profile
	 * @return the WORK_DIR value
	 */
	public IZosUNIXFile getWorkingDirectory();
	
	/**
	 * Returns the JVM server JVMLOG file
	 * @return JVMLOG {@link IZosUNIXFile}
	 */
	public IZosUNIXFile getJvmLogFile();

	/**
	 * Returns the JVM server STDOUT file
	 * @return STDOUT {@link IZosUNIXFile}
	 */
	public IZosUNIXFile getStdOutFile();
	
	/**
	 * Returns the JVM server STDERR file
	 * @return STDERR {@link IZosUNIXFile}
	 */
	public IZosUNIXFile getStdErrFile();
	
	/**
	 * Returns the JVM server JVMTRACE file
	 * @return JVMTRACE {@link IZosUNIXFile}
	 */
	public IZosUNIXFile getJvmTraceFile();
	
	/**
	 * Save a checkpoint of the current state of the JVM server log files
	 * @return the key to the checkpoint
	 * @throws CicsJvmserverResourceException 
	 */
	public String checkpointLogs() throws CicsJvmserverResourceException;

	/**
	 * Returns the contents of a specified log file
	 * @param logFile the logFile retrieve
	 * @return contents of logFile
	 * @throws CicsJvmserverResourceException 
	 */
	public OutputStream getLogFile(IZosUNIXFile logFile) throws CicsJvmserverResourceException;
	
	/**
	 * Returns the contents of a specified log file since the supplied checkpoint
	 * @param logFile the logFile retrieve
	 * @param checkpoint value
	 * @return contents of logFile
	 * @throws CicsJvmserverResourceException 
	 */
	public OutputStream getLogFileSinceCheckpoint(IZosUNIXFile logFile, String checkpoint) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of supplied log file for specified text
	 * @param logFile the logFile retrieve
	 * @param text the text to search
	 * @return true if text found
	 * @throws CicsJvmserverResourceException 
	 */
	public boolean searchLogFileForText(IZosUNIXFile logFile, String searchText) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of supplied log file for specified text since the supplied checkpoint
	 * @param logFile the logFile retrieve
	 * @param searchText the text to search
	 * @param checkpoint value
	 * @return true if text found
	 * @throws CicsJvmserverResourceException 
	 */
	public boolean searchLogFileForTextSinceCheckpoint(IZosUNIXFile logFile, String searchText, String checkpoint) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of supplied log file for specified Pattern
	 * @param logFile the logFile retrieve
	 * @param searchPattern the Pattern to search
	 * @return true if text found
	 * @throws CicsJvmserverResourceException 
	 */
	public boolean searchLogFileForPattern(IZosUNIXFile logFile, Pattern searchPattern) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of supplied log file for specified text since the supplied checkpoint
	 * @param logFile the logFile retrieve
	 * @param searchPattern the Pattern to search
	 * @param checkpoint value
	 * @return true if text found
	 * @throws CicsJvmserverResourceException 
	 */
	public boolean searchLogFileForPatternSinceCheckpoint(IZosUNIXFile logFile, Pattern searchPattern, String checkpoint) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search text to appear in specified log file. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found;</li>
	 * <li>the failText is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul> 
	 * @param logFile the logFile to search
	 * @param searchString the text to search
	 * @param millisecondTimeout timeout value
	 * @return true if the string is found
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForTextInLogFile(IZosUNIXFile logFile, String searchText, long millisecondTimeout) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search text or fail text to appear in specified log file. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found;</li>
	 * <li>the failText is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul> 
	 * @param logFile the logFile to search
	 * @param searchString the text to search
	 * @param failString the failure text to search
	 * @param millisecondTimeout timeout value
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForTextInLogFile(IZosUNIXFile logFile, String searchText, String failText, long millisecondTimeout) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search text to appear in specified log file since the supplied checkpoint. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found;</li>
	 * <li>the failText is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul>
	 * @param logFile the logFile to search
	 * @param searchText the text to search
	 * @param millisecondTimeout timeout value
	 * @param checkpoint the checkpoint key
	 * @return true if the string is found
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForTextLogFileSinceCheckpoint(IZosUNIXFile logFile, String searchText, long millisecondTimeout, String checkpoint) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search text to appear in specified log file since the supplied checkpoint. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found;</li>
	 * <li>the failText is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul>
	 * @param logFile the logFile to search
	 * @param searchText the text to search
	 * @param failText the failure text to search
	 * @param millisecondTimeout timeout value
	 * @param checkpoint the checkpoint key
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForTextLogFileSinceCheckpoint(IZosUNIXFile logFile, String searchText, String failText, long millisecondTimeout, String checkpoint) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search Pattern to appear in specified log file. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found;</li>
	 * <li>the failPattern is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul>
	 * @param logFile the logFile to search
	 * @param searchPattern the Pattern to search
	 * @param millisecondTimeout timeout value
	 * @return true if the string is found
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForPatternInLogFile(IZosUNIXFile logFile, Pattern searchPattern, long millisecondTimeout) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search Pattern or fail Pattern to appear in specified log file. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found;</li>
	 * <li>the failPattern is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul>
	 * @param logFile the logFile to search
	 * @param searchPattern the Pattern to search
	 * @param failPattern the failure pattern to search
	 * @param millisecondTimeout timeout value
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForPatternInLogFile(IZosUNIXFile logFile, Pattern searchPattern, Pattern failPattern, long millisecondTimeout) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search Pattern to appear in specified log file since the supplied checkpoint. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found;</li>
	 * <li>the failPattern is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul>
	 * @param logFile the logFile to search
	 * @param searchPattern the Pattern to search
	 * @param millisecondTimeout timeout value
	 * @param checkpoint the checkpoint key
	 * @return true if the string is found
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForPatternLogFileSinceCheckpoint(IZosUNIXFile logFile, Pattern searchPattern, long millisecondTimeout, String checkpoint) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search Pattern or fail Pattern to appear in specified log file since the supplied checkpoint. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found;</li>
	 * <li>the failPattern is found;</li>
	 * <li>the specified millisecondTimeout is reached.</li>
	 * </ul>
	 * @param logFile the logFile to search
	 * @param searchPattern the Pattern to search
	 * @param failPattern the failure pattern to search
	 * @param millisecondTimeout timeout value
	 * @param checkpoint the checkpoint key
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForPatternLogFileSinceCheckpoint(IZosUNIXFile logFile, Pattern searchPattern, Pattern failPattern, long millisecondTimeout, String checkpoint) throws CicsJvmserverResourceException;

	/**
     * Store the content of the JVM server log files to the default location in the Results Archive Store
     * @throws CicsJvmserverResourceException
     */
    public void saveToResultsArchive() throws CicsJvmserverResourceException;
	
	/**
	 * Store the content of the JVM server log files to the Results Archive Store
	 * @param rasPath path in Results Archive Store
	 * @throws CicsJvmserverResourceException
	 */
	public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException;

	/**
	 * Store the content of the JVM server log files to the default location in the Results Archive Store and delete the files
     * @param rasPath path in Results Archive Store
     * @throws CicsJvmserverResourceException
	 */
	public void clearJvmLogs() throws CicsJvmserverResourceException;

	/**
	 * Store the content of the JVM server log files to the Results Archive Store and delete the files
     * @param rasPath path in Results Archive Store
     * @throws CicsJvmserverResourceException
	 */
	public void clearJvmLogs(String rasPath) throws CicsJvmserverResourceException;
	
	/**
	 * Run the JVM gather diagnostics script on zOS UNIX to gather diagnostics for the JVM server. The diagnostics are
	 * added to a tar file and save to the default location in the Results Archive Store 
	 * @throws CicsJvmserverResourceException 
	 */
	public void gatherDiagnostics() throws CicsJvmserverResourceException;	
	
	/**
	 * Run the JVM gather diagnostics script on zOS UNIX to gather diagnostics for the JVM server. The diagnostics are
	 * added to a tar file and save to the default location in the Results Archive Store
	 * @param rasPath path in Results Archive Store
	 * @throws CicsJvmserverResourceException 
	 */
	public void gatherDiagnostics(String rasPath) throws CicsJvmserverResourceException;
}
