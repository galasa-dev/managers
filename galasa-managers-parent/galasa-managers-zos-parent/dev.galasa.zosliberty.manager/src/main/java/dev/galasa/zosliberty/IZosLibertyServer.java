/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

import java.io.OutputStream;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosUNIXFile;

public interface IZosLibertyServer {
	
	public enum ApplicationType {
		EAR("ear"),
		EBA("eba"),
		EJB("ejb"),
		WAR("war");

		private final String type;
		
		ApplicationType(String type) {
			this.type = type;
		}
		
		@Override
		public String toString() {
			return this.type;
		}
	}

    /**
     * Provides the Liberty Server Name
     * @param serverName 
     * @throws ZosLibertyServerException
     */
    public void setServerName(String serverName);
    
    /**
     * Provides the location of the Liberty Install Directory <code>${WLP_INSTALL_DIR}</code>
     * @param wlpInstallDir
     * @throws ZosLibertyServerException
     */
    public void setWlpInstallDir(String wlpInstallDir) throws ZosLibertyServerException;

    /**
     * Provides the location of the Liberty User Directory <code>${WLP_USER_DIR}</code>
     * @param wlpUserDir
     * @throws ZosLibertyServerException
     */
    public void setWlpUserDir(String wlpUserDir) throws ZosLibertyServerException;
    
    /**
     * Provides the location of the Liberty Output Directory <code>${WLP_OUTPUT_DIR}</code>
     * @param wlpOutputDir
     * @throws ZosLibertyServerException
     */
    public void setWlpOutputDir(String wlpOutputDir) throws ZosLibertyServerException;
    
    /**
     * Set the value of JAVA_HOME for this Liberty server
     * @param javaHome
     * @throws ZosLibertyServerException 
     */
    public void setJavaHome(String javaHome) throws ZosLibertyServerException;
	
	/**
	 * Returns the Liberty version
	 * @return Liberty version
	 * @throws ZosLibertyServerException 
	 */
	public String getVersion() throws ZosLibertyServerException;

	/**
     * Returns the Liberty Server Name
     * @return the Liberty Server Name
     */
    public String getServerName();
    
    /**
     * Returns the Liberty Install Directory <code>${WLP_INSTALL_DIR}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Install Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpInstallDir() throws ZosLibertyServerException;
    
    /**
     * Returns the Liberty User Directory <code>${WLP_USER_DIR}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty User Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpUserDir() throws ZosLibertyServerException;
    
    /**
     * Returns the Liberty Output Directory <code>${WLP_OUTPUT_DIR}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Output Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpOutputDir() throws ZosLibertyServerException;
    
    /**
     * Returns the Liberty Shared Application Directory <code>${shared.app.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Shared Application Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getSharedAppDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Server Configuration Directory <code>${server.config.app.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Server Configuration Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getServerConfigDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Server Output Directory <code>${server.output.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Server Output Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getServerOutputDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Shared Configuration Directory <code>${shared.config.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Shared Configuration Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getSharedConfigDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Shared Resources Directory <code>${shared.resources.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Shared Resources Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getSharedResourceDir() throws ZosLibertyServerException;

	/**
	 * Get the Liberty server logs directory
	 * @return the Liberty server logs directory
	 * @throws ZosLibertyServerException 
	 */
	public IZosUNIXFile getLogsDirectory() throws ZosLibertyServerException; //TODO: ????

	/**
	 * Get the Liberty server dropins directory
	 * @return the dropins directory
	 * @throws ZosLibertyServerException
	 */
	public IZosUNIXFile getDropinsDir() throws ZosLibertyServerException;

	/**
	 * Get the JAVA_HOME value for this Liberty server
	 * @return
	 * @throws ZosLibertyServerException
	 */
	public IZosUNIXFile getJavaHome() throws ZosLibertyServerException;
	
	/**
	 * Get the zOS Image for this Liberty server
	 * @return
	 */
	public IZosImage getZosImage();

	/**
     * Set the server.xml object for this Liberty server 
     * @param serverXml the {@link IZosLibertyServerXml} server.xml object
     * @throws ZosLibertyServerException
     */
    public void setServerXml(IZosLibertyServerXml serverXml) throws ZosLibertyServerException;
    
    /**
     * Get the server.xml object for this Liberty server
     * @return the server.xml
     * @throws ZosLibertyServerException
     */
    public IZosLibertyServerXml getServerXml() throws ZosLibertyServerException;
    
    /**
     * Load the content of the Liberty server.xml from the file system into the internal {@link IZosLibertyServerXml} object 
     * @return the server.xml
     * @throws ZosLibertyServerException
     */
    public IZosLibertyServerXml loadServerXmlFromFileSystem() throws ZosLibertyServerException;
    
    /**
	 * Get the contents of the Liberty servers logs directory
	 * @return the Liberty server logs
	 * @throws ZosLibertyServerException
	 */
	public IZosLibertyServerLogs getLogs() throws ZosLibertyServerException ;

	/**
	 * Get the current contents of the Liberty server <code>messages.log</code> as a {@link String}
	 * @return the content of the <code>messages.log</code>
	 * @throws ZosLibertyServerException
	 */
	public String getMessageLog() throws ZosLibertyServerException;	
	
	/**
	 * Builds the the Liberty server on the zOS UNIX file system from any objects that have been created such as the server.xml
	 * @throws ZosLibertyServerException
	 */
	public void build() throws ZosLibertyServerException;	

	/**
     * Create a Liberty server using the <code>server create</code> command 
     * @return <code>server</code> command return code
     * @throws ZosLibertyServerException
     */
    public int create() throws ZosLibertyServerException;

    /**
     * Start the Liberty server using the <code>server start</code> command
     * @return <code>server</code> command return code
     * @throws ZosLibertyServerException
     */
    public int start() throws ZosLibertyServerException;

    /**
     * Run the Liberty server as a zOS batch job
     * @return <code>server</code> command return code
     * @throws ZosLibertyServerException
     */
    public int run() throws ZosLibertyServerException;
    
    /**
	 * Wait for the Liberty server to start using the default timeout
	 * @return the return code from the <code>server status</code> command
	 * @throws ZosLibertyServerException
	 */
	public int waitForStart() throws ZosLibertyServerException;

	/**
	 * Wait for the Liberty server to start using the supplied timeout
	 * @param millisecondTimeout the timeout in milliseconds
	 * @return the return code from the <code>server status</code> command
	 * @throws ZosLibertyServerException
	 */
	public int waitForStart(int millisecondTimeout) throws ZosLibertyServerException;

	/**
	 * Wait for the Liberty server to issue the <code>CWWKF0011I</code> message to the <code>messages.log</code> using the default timeout
	 * @return true if message was found in log
	 * @throws ZosLibertyServerException
	 */
	public boolean waitForStartMessage() throws ZosLibertyServerException;

	/**
	 * Wait for the Liberty server to issue the <code>CWWKF0011I</code> message to the <code>messages.log</code> using the supplied timeout
	 * @param millisecondTimeout the timeout in milliseconds
	 * @return true if message was found in log
	 * @throws ZosLibertyServerException
	 */
	public boolean waitForStartMessage(int millisecondTimeout) throws ZosLibertyServerException;

	/**
     * Stop the Liberty server
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int stop() throws ZosLibertyServerException;
    
    /**
	 * Wait for the Liberty server to stop using the default timeout
	 * @return the return code from the <code>server status</code> command
	 * @throws ZosLibertyServerException
	 */
	public int waitForStop() throws ZosLibertyServerException;

	/**
	 * Wait for the Liberty server to stop using the supplied timeout
	 * @param millisecondTimeout the timeout in milliseconds
	 * @return the return code from the <code>server status</code> command
	 * @throws ZosLibertyServerException
	 */
	public int waitForStop(int millisecondTimeout) throws ZosLibertyServerException;

	/**
     * Get the Liberty sever status using the <code>server status</code> command
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int status() throws ZosLibertyServerException;
    
    /**
     * Delete the Liberty server files and directories
     * @throws ZosLibertyServerException
     */
	public void delete() throws ZosLibertyServerException;
	
	/**
	 * Convenience method to create or replace the <code>defaultHttpEndpoint</code> element in the Liberty server server.xml
	 * @param host the value for the <code>host</code> property. The property will not be set when the value is null
	 * @param httpPort the value of the <code>httpPort</code> property. The property will not be set when the value less than 0 
	 * @param httpsPort the value of the <code>httpPorts</code> property. The property will not be set when the value less than 0
	 * @throws ZosLibertyServerException
	 */
	public void setDefaultHttpEndpoint(String host, int httpPort, int httpsPort) throws ZosLibertyServerException;
	
	/**
	 * Deploy an application to the Liberty server. This method copies the application archive file to the zOS UNIX file system and
	 * creates an <code>application</code> element in the server.xml.
	 * @param clazz a {@link class} in the same bundle containing the application archive file
	 * @param path the path in the bundle to the application archive file
	 * @param targetLocation the location on the zOS UNIX file system to store the application archive file. If the value is null, 
	 * <code>${shared.app.dir}/fileName</code> will be used
	 * @param type the application type {@see ApplicationType}
	 * @param name the name of the application
	 * @param contextRoot the application context-root. Can be null
	 * @throws ZosLibertyServerException
	 */
	public void deployApplication(Class<?> clazz, String path, String targetLocation, ApplicationType type, String name, String contextRoot) throws ZosLibertyServerException;
	
	/**
	 * Deploy an application to the Liberty server dropins directory. This method copies the application archive file to the Liberty server dropins directory
	 * @param clazz a {@link class} in the same bundle containing the application archive file
	 * @param path the path in the bundle to the application archive file
	 * @throws ZosLibertyServerException
	 */
	public void deployApplicationToDropins(Class<?> clazz, String path) throws ZosLibertyServerException;
	
	/**
	 * Remove an application from the Liberty server. This method removes the <code>application</code> from the server.xml and
	 * deletes the application archive file from the zOS UNIX file system. Set name to <code>null</code> to remove all applications from dropins
	 * @param name the application name
	 * @throws ZosLibertyServerException
	 */
	public void removeApplication(String name) throws ZosLibertyServerException;
	
	/**
	 * Remove an application from the Liberty server dropins directory. Set fileName to <code>null</code> to remove all applications from dropins
	 * @param fileName the name of the file in the dropins directory
	 * @throws ZosLibertyServerException
	 */
	public void removeApplicationFromDropins(String fileName) throws ZosLibertyServerException;
	
	/**
	 * Wait for an application to start using the default timeout by looking for <code>CWWKZ0001I</code> message in messages.log.
	 * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
	 * @param name the application name
	 * @return true if application has started within the timeout
	 * @throws ZosLibertyServerException
	 */
	public boolean waitForApplicationStart(String name) throws ZosLibertyServerException;
	
	/**
	 * Wait for an application to start using the supplied timeout by looking for <code>CWWKZ0001I</code> message in messages.log.
	 * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
	 * @param name the application name
	 * @param millisecondTimeout the timeout in milliseconds
	 * @return true if application has stopped within the timeout
	 * @throws ZosLibertyServerException
	 */
	public boolean waitForApplicationStart(String name, int millisecondTimeout) throws ZosLibertyServerException;
	
	/**
	 * Wait for an application to stop using the default timeout by looking for <code>CWWKZ0009I</code> message in messages.log.
	 * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
	 * @param name the application name
	 * @return true if application has started within the timeout
	 * @throws ZosLibertyServerException
	 */
	public boolean waitForApplicationStop(String name) throws ZosLibertyServerException;
	
	/**
	 * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
	 * @param name the application name
	 * @param millisecondTimeout the timeout in milliseconds
	 * @return true if application has stopped within the timeout
	 * @throws ZosLibertyServerException
	 */
	public boolean waitForApplicationStop(String name, int millisecondTimeout) throws ZosLibertyServerException;
	
	void startTODO(); //TODO

	/**
     * Saves the JVM Options file to the stored artifacts
     * 
     * @throws ZosLibertyServerException
     */
    public void saveJvmOptions() throws ZosLibertyServerException;
    
    /**
     * Inserts a checkpoint string into all of the logs for the related Liberty JVM server.
     * This includes the messages log which is excluded from the parent class' checkpoint
     * process.
     * 
     * @param binary - Specifies whether or not to checkpoint the logs in binary format.
     * @throws ZosLibertyServerException
     */

    public String checkpointLogs() throws ZosLibertyServerException;
    
    /**
     * Gets the contents of the Liberty server's message log since the last specified checkpoint
     * as an OutputStream object.
     * 
     * NOTE - Liberty maintains log integrity, preventing us from writing a checkpoint string
      * instead we save the last timestamp in the file at the time of the checkpoint request
      * and use that as our checkpoint. This java object will keep track of that checkpoint for you.
      * 
     * @param binary - Note: This should ALWAYS be true. <code>messages.log</code> is always in binary
     * @return - The contents of the log after the checkpoint as an OutputStream object
     * @throws ZosLibertyServerException
     */
    public OutputStream getMsgLogSinceCheckpoint(boolean binary) throws ZosLibertyServerException;
    
    /**
     * Waits for the specified amount of time for a message to appear in the Liberty server's
     * message logs. If the message doesn't appear, or the failing message appears in the log,
     * then the method will return false. The method will only check for instances of the message
     * which appear after the latest checkpoint if useCheckpoint is set to true.
     * 
     * NOTE - Liberty maintains log integrity, preventing us from writing a checkpoint string
      * instead we save the last timestamp in the file at the time of the checkpoint request
      * and use that as our checkpoint. This java object will keep track of that checkpoint for you.
     * 
     * @param message - The string we wish to wait for in the logs
     * @param failMessage - The message that indicates an error or problem has occurred
     * @param resourceTimeout - The amount of time in milliseconds to wait
     * @param useCheckpoint - Boolean value, true uses the liberty checkpoint, otherwise the method will use the entire log.
     * @return - True or false, true if the message was found. Otherwise returns false
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean waitForMessageInMsgLog(String message, String failMessage, int resourceTimeout, boolean useCheckpoint) throws ZosLibertyServerException;
    
    /**
     * Waits for the specified amount of time for a message to appear in the Liberty server's
     * message logs. If the message doesn't appear then the method will return false.
     * 
     * @param message - The message we wish to wait for.
     * @return - True or false. True if the sting is found, false in any other case.
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean waitForMessageInMsgLog(String message) throws ZosLibertyServerException;
    
    /**
     * Waits for the specified amount of time for a message to appear in the Liberty server's
     * message logs. If the message doesn't appear then the method will return false.
     * 
     * @param message - The message we wish to wait for.
     * @param resourceTimeout - The amount of time in milliseconds we wish to wait for
     * @return - True or false. True if the sting is found, false in any other case.
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean waitForMessageInMsgLog(String message, int resourceTimeout) throws ZosLibertyServerException;
    
    /**
     * Searches the message log for a specified string. 
     * 
     * @param stringValue - The string we wish to find
     * @param binary - Specifies if the user wishes to use binary to read the log 
     * @return - True if the string is found, otherwise false.
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean searchMessageLogForString(String stringValue, boolean binary) throws ZosLibertyServerException;
    
    /**
     * Searches the message log for a specified string since the  last checkpoint specified in the
     * parameters.
     * 
     * NOTE - Liberty maintains log integrity, preventing us from writing a checkpoint string
      * instead we save the last timestamp in the file at the time of the checkpoint request
      * and use that as our checkpoint. This java object will keep track of that checkpoint for you.
     *
     * @param stringValue - The string we wish to find
     * @param binary - Specifies if the user wishes to use binary to read the log
     * @return - True if the string is found, otherwise false
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean searchMessageLogForStringSinceCheckpoint(String stringValue, boolean binary) throws ZosLibertyServerException;
    
    /**
     * Searches the log for text which matches the regular expression pattern specified in the
     * parameter 'text'. 
     * 
     * @param text - The regular expression you wish to match
     * @param binary - Specifies whether to read the logs in binary or not.
     * @return - True if the pattern is matched, otherwise false.
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean searchMessageLogForText(String text, boolean binary) throws ZosLibertyServerException;
    
    /**
     * Searches the log since the specified checkpoint for matches to the regular expression
     * pattern specified in the parameter 'text'.
     * 
     * NOTE - Liberty maintains log integrity, preventing us from writing a checkpoint string
      * instead we save the last timestamp in the file at the time of the checkpoint request
      * and use that as our checkpoint. This java object will keep track of that checkpoint for you.
     * 
     * @param text - The regular expression you wish to match
     * @param binary - Specifies whether or not to read the logs in binary
     * @return - True if the pattern is matched, otherwise false
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean searchMessageLogForTextSinceCheckpoint(String text, boolean binary) throws ZosLibertyServerException;
    
    /**
     * Waits for a specified number of milliseconds for a specified message to appear in the Liberty
     * message logs. Only the most recent <code>messages.log</code> file will be checked. All previous, now timestamped,
     * message logs will be ignored.
     * 
     * @param message - The message we wish to search for
     * @param failMessage - A message that indicates our search will fail
     * @param resourceTimeout - The amount of time we wish to wait for the message to appear
     * @param useCheckpoint - Specifies whether we should wait from the latest checkpoint onwards
     * @return boolean value, true if the message is found in the specified time, otherwise false
     * @throws ZosLibertyServerException
     * @throws MVSException
     * @throws EnvironmentException
     */
    public boolean waitForMessageInLatestMsgLog(String message, String failMessage, int resourceTimeout, boolean useCheckpoint) throws ZosLibertyServerException;

    /**
     * Runs the command './wlpenv server dump' from the workdir which will take a Liberty server dump.
     * This dump is then saved as a downloadable zip in the test run's stored artifacts.
     * @param desiredDumpName This is the name that the dump will be stored under in the test's stored artifacts, set this to null to let the dump name default to serverName.dump.timestamp
     * @throws MVSException 
     * @throws EnvironmentException 
     * @throws  
     */
    public void getLibertyServerDump(String desiredDumpName) throws ZosLibertyServerException;


    /**
     * Save all trace files in logs directory
     */
    public void saveLibertyTraceFiles();
    
    /**
     * @return The Liberty keystore file. <code>null</code> if the file cannot be
     *         found.
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getKeystoreFile() throws ZosLibertyServerException;

    /**
     * Write the Liberty jvm.options to USS
     * @param options 
     * 
     * @throws ZosLibertyServerException
     */
    public void writeJvmOptionsUSS(String options) throws ZosLibertyServerException;

        
    /**
     * Clear an existing WLP workarea
     * 
     * @throws ZosLibertyServerException 
     */
    public void clearWorkarea() throws ZosLibertyServerException;

    /**
     * Delete the Liberty server logs
     * @throws ZosLibertyServerException
     */
    public void clearLogs() throws ZosLibertyServerException;
    
    /**
     * Use the Liberty securityUtility command to encode a password
     * @param password - The password to encode
     * @return - The encoded password
     * @throws ZosLibertyServerException 
     * @throws CICSManagerException 
     */
    public String securityUtilityEncode(String password) throws ZosLibertyServerException;
        
    /**
     * Use the Liberty securityUtility to create a new keyStore
     * @param cics <p>The CICS region that this WLP is inside (only way we can get applid and temp directory from here)</p>
     * @param password <p>The password to create the certificate/keystore with
     * @return <p>Full path to the new keystore file</p>
     * @throws ZosLibertyServerException
     */
    public String securityGenerateKeystore(/*ICICS cics,*/ String password) throws ZosLibertyServerException;

	void endTODO(); // TODO

	/**
     * Store the content of the Liberty server logs and configuration to the default location in the Results Archive Store
     * @throws ZosLibertyServerException
     */
    public void saveToResultsArchive() throws ZosLibertyServerException;
	
	/**
	 * Store the content of the Liberty server logs and configuration to the Results Archive Store
	 * @param rasPath path in Results Archive Store
	 * @throws ZosLibertyServerException
	 */
	public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException;
}
