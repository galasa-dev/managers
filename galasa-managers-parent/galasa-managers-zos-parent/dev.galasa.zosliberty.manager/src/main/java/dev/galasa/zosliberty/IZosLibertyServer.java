/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;

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
     * Provides the location of the Liberty Install Directory ($WLP_INSTALL_DIR)
     * @param wlpInstallDir
     * @throws ZosLibertyServerException
     */
    public void setWlpInstallDir(String wlpInstallDir) throws ZosLibertyServerException;

    /**
     * Provides the location of the Liberty User Directory ($WLP_USER_DIR)
     * @param wlpUserDir
     * @throws ZosLibertyServerException
     */
    public void setWlpUserDir(String wlpUserDir) throws ZosLibertyServerException;
    
    /**
     * Provides the location of the Liberty Output Directory ($WLP_OUTPUT_DIR)
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
     * Returns Liberty Server Name
     * @return the Liberty Server Name
     */
    public String getServerName();
    
    /**
     * Returns Liberty Install Directory ($WLP_INSTALL_DIR) as a {@link IZosUNIXFile} object
     * @return Liberty Install Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpInstallDir() throws ZosLibertyServerException;
    
    /**
     * Returns Liberty User Directory ($WLP_USER_DIR) as a {@link IZosUNIXFile} object
     * @return Liberty User Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpUserDir() throws ZosLibertyServerException;
    
    /**
     * Returns Liberty Output Directory ($WLP_OUTPUT_DIR) as a {@link IZosUNIXFile} object
     * @return Liberty Output Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpOutputDir() throws ZosLibertyServerException;
    
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
     * @return 
     * @throws ZosLibertyServerException
     */
    public IZosLibertyServerXml loadServerXmlFromFileSystem() throws ZosLibertyServerException;
    
    public int create() throws ZosLibertyServerException;

    public int start() throws ZosLibertyServerException;

    public boolean run() throws ZosLibertyServerException;
    
    public int stop() throws ZosLibertyServerException;
    
    public int status() throws ZosLibertyServerException;
    
    public int waitForStart() throws ZosLibertyServerException;

	public int waitForStart(int millisecondTimeout) throws ZosLibertyServerException;

	public int waitForStop() throws ZosLibertyServerException;
	
	public int waitForStop(int millisecondTimeout) throws ZosLibertyServerException;
	
	public void delete() throws ZosLibertyServerException;
	
	public void setDefaultHttpEndpoint(String host, int httpPort, int httpsPort) throws ZosLibertyServerException;
	
	public void deployApplication(String path, Class<?> clazz, String targetLocation, ApplicationType type, String name, String contextRoot) throws ZosLibertyServerException;
	
	public void deployApplicationToDropins(String path, Class<?> clazz) throws ZosLibertyServerException;
	
	public IZosUNIXFile getDropinsDir() throws ZosLibertyServerException;
    
    
//TODO: vvvvv review these methods vvvvv    
    
    
    
    
    
    
    
    
    /**
     * Recovers the contents of the messages.log file for this server
     * 
     * @throws ZosLibertyServerException
     * 
     * @return String - String containing the contents of the server's messages.log file
    */
    public String getMessageLog() throws ZosLibertyServerException;
    
    /** Adds an application tag to a Liberty server's 'server.xml'
     * based upon the users specifications.
     * 
     * @param path - Path to the bundle being added
     * @param id - Id of the bundle
     * @param name - Name of the bundle
     * @param type - Type of bundle (e.g. eba)
     * 
     * @throws ZosLibertyServerException
     */
    public void addApplicationTag(String path, String id, String name, String type) throws ZosLibertyServerException;

    /**
     * Enables dropins for this liberty server
     * 
     * @throws ZosLibertyServerException
     */
    public void enableDropins() throws ZosLibertyServerException;
    
    /**
     * Adds a provided bundle to the dropins directory in Liberty
     * 
     * @param bundle - Path in USS of bundle you wish to add.
     * @param targetFileName - The new name of the file (NOT the full path, so for example bundle.ebabundle)
     * @throws ZosLibertyServerException
     */
    public void addBundleToDropins(String bundle, String targetFileName) throws ZosLibertyServerException;
    
    /**
     * Removes a specified bundle from the dropins directory in Liberty.
     * 
     * @param bundleName - Name of bundle to be removed. Doesn't need full location.
     * @throws ZosLibertyServerException
     */
    public void removeBundleFromDropins(String bundleName) throws ZosLibertyServerException;

    /**
     * Tries to remove all files from the dropins directory of the Liberty JVM server.
     * 
     * @throws ZosLibertyServerException
     */
    public void cleanDropins() throws ZosLibertyServerException;
    
    /**
     * Returns a string containing the full path to the directory used to store logs
     * for this Liberty JVM server.
     *
     * @return String - String containing the full path to the server's logs directory
     * @throws ZosLibertyServerException 
     */
    //TODO Javadoc
    public IZosUNIXFile getLogsDirectory() throws ZosLibertyServerException;
    
    /**
     * Returns a string containing the full path to the server.xml config file for 
     * this Liberty JVM Server
     * 
     * @return String - containing full path to the server's server.xml file
     */
    public String getWlpServerConfigPath();
    
    
    /**
     * Builds, defines and installs a liberty server object. Differs from JVMServer building in
     * that it checks the liberty message logs for the liberty start up message CWWKF0011I before
     * checking the CICS status.
     * 
     * @throws ZosLibertyServerException
     */
    public void build() throws ZosLibertyServerException;

    /**
     * Returns the Liberty version
     * @return Liberty version
     * @throws ZosLibertyServerException 
     */
    public String getVersion() throws ZosLibertyServerException;
    
    /**
     * Build a Liberty Server from the shipped templates
     * 
     * @throws ZosLibertyServerException
     */
    public void buildWlpServerFromTemplate() throws ZosLibertyServerException;    
    
    
    /**
     * Like {@link IWLPServer#buildWlpServerBasic(String, int, int, String)} but using a pre-prepared server.xml
     * 
     * @param serverXMLArtifactPath <p>The full path to the server.xml artifact in the test bundle</p>
     * @param serverName <p>The name of the wlpServer to build and configure</p>
     * @param klass <p>The test class</p>
     * @throws ZosLibertyServerException
     */
    public void buildWlpServerWithServerXML(String serverXMLArtifactPath, String serverName, Class<?> klass) throws ZosLibertyServerException;
    
    /**
     * Build a basic WLP directory structure and server.xml
     * 
     * @param serverDescription - Description of the server
     * @param httpPort - Http port
     * @param httpsPort - ssl port
     * @param hostName - host name to bind to
     * @throws ZosLibertyServerException
     */
    public void buildWlpServerBasic(String serverDescription, int httpPort, int httpsPort, String hostName) throws ZosLibertyServerException;


    /**
     * Saves the server.xml file to the stored artifacts
     */
    public void saveServerXML() ;
    
    /**
     * @throws ZosLibertyServerException 
     */
    public void saveInstalledAppsXML() throws ZosLibertyServerException ;
    
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
     * Inserts a checkpoint string into all of the logs for the related Liberty JVM server.
     * This includes the messages log which is excluded from the parent class' checkpoint
     * process.
     * 
     * @param output - String that will be added to the checkpoint
     * @return checkpoint string value that was created and added to the logs
     * @throws ZosLibertyServerException
     */
    public String checkpointLogs(String output) throws ZosLibertyServerException;
    
    /**
     * Gets the contents of the Liberty server's message log as an OutputStream
     * 
     * @param binary - Indicates whether or not the user wishes to get the logs in binary
     * @return - OutputStream object containing contents of the log
     * @throws ZosLibertyServerException
     */
    public OutputStream getMsgLog(boolean binary) throws ZosLibertyServerException;
    
    /**
     * Gets the contents of the Liberty server's message log since the last specified checkpoint
     * as an OutputStream object.
     * 
     * NOTE - Liberty maintains log integrity, preventing us from writing a checkpoint string
      * instead we save the last timestamp in the file at the time of the checkpoint request
      * and use that as our checkpoint. This java object will keep track of that checkpoint for you.
      * 
     * @param binary - Note: This should ALWAYS be true. messages.log is always in binary
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
    public boolean waitForMessageInMsgLog(String message, String failMessage, long resourceTimeout, boolean useCheckpoint) throws ZosLibertyServerException;
    
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
    public boolean waitForMessageInMsgLog(String message, long resourceTimeout) throws ZosLibertyServerException;
    
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
     * Waits for the liberty server to start up by checking the messages log. The method will
     * wait for the starting message to appear, identifying it by the message id 'CWWKF0011I'.
     * 
     * @return - True if the server started successfully, otherwise false.
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean waitForLibertyEnabled() throws ZosLibertyServerException;
    
    /**
     * Waits for the liberty server to stop by checking the messages log. The method will
     * wait for the stop message to appear, identifying it by the message id 'CWWKE0036I'.
     * 
     * @return - True if the server started successfully, otherwise false.
     * @throws ZosLibertyServerException
     * @throws EnvironmentException 
     * @throws MVSException 
     */
    public boolean waitForLibertyDisabled() throws ZosLibertyServerException;
    
    /**
     * Collects up all the log files in the liberty logs directory and reads them all in as if
     * they were one file. If the user specifies the use of a checkpoint then only the information
     * that exists after the liberty checkpoint stored by the LibertyJVMServer object will be returned.
     * 
     * @param useCheckpoint - boolean value specifying whether or not use a checkpoint
     * @return - Contents of all log files from the desired point
     * @throws EnvironmentException
     * @throws MVSException
     * @throws ZosLibertyServerException 
     */
    public OutputStream getOrderedMessageLogsContents(boolean useCheckpoint) throws  ZosLibertyServerException;

    /**
     * Collects up all the log files in the liberty logs directory and reads them all in as if
     * they were one file. 
     * 
     * @return - Contents of all log files as OutputStream
     * @throws EnvironmentException
     * @throws MVSException
     * @throws ZosLibertyServerException
     */
    public OutputStream getOrderedMessageLogsContents() throws  ZosLibertyServerException;

    /**
     * Gets the contents of only the latest messages.log file for use in checking for messages 
     * or looking for information.
     * 
     * @return - OutputStream with contents of the latest messages.log file.
     * @throws ZosLibertyServerException
     */
    public OutputStream getLatestMessageLogContents() throws ZosLibertyServerException;
    
    /**
     * Searches only the latest messages.log file for the value of 'stringValue', returning true if it
     * finds the string, or false if it doesn't.
     * 
     * @param stringValue The string we wish to find in our logs
     * @return True if the string is found, otherwise false
     * @throws ZosLibertyServerException
     * @throws MVSException
     * @throws EnvironmentException
     */
    public boolean searchLatestMessageLogForString(String stringValue) throws ZosLibertyServerException;
    
    /**
     * Attempts to match messages in the latest messages.log file with the regular expression provided as
     * the value of 'text'. The method will return true if a match is found, or false if one isn't.
     * 
     * @param text The regular expression we wish to search for
     * @return True if we find a match, otherwise false
     * @throws ZosLibertyServerException
     * @throws MVSException
     * @throws EnvironmentException
     */
    public boolean searchLatestMessageLogForText(String text) throws ZosLibertyServerException;
    
    /**
     * Waits for a specified number of milliseconds for a specified message to appear in the Liberty
     * message logs. Only the most recent messages.log file will be checked. All previous, now timestamped,
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
    public boolean waitForMessageInLatestMsgLog(String message, String failMessage, long resourceTimeout, boolean useCheckpoint) throws ZosLibertyServerException;

    /**
     * Transfers a bundle from the project workspace to the dropins directory of the issuing Liberty JVM server. The 
     * method will begin it's search starting at the resource directory of the owning project. Paths passed in should be
     * written as relative to this directory. The transfer of the files will be done in binary.
     * 
     * @param bundleLocation - Location of the bundle relative to the resources directory in the project
     * @param bundleFileName - The desired name of bundle once it reaches USS (this will override the existing file name)
     * @param owningClass - Requirement of JAT+, used to located the resources folder. 'this.class()' usually suffices as a parameter.
     * 
     * @throws ZosLibertyServerException
     * @throws MVSException
     * @throws EnvironmentException
     */
    public void addLocalBundleToDropins(String bundleLocation, String bundleFileName, Class<?> owningClass) throws ZosLibertyServerException;

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
     * Checks if the Liberty JVM server contains any FFDCs.
     * <p>
     * Identical to calling {@link #containsFFDCs(boolean) containsFFDCs(false)}.
     * 
     * @return <code>true</code> if FFDCs are found.
     * @throws ZosLibertyServerException
     */
    public boolean containsFFDCs() throws ZosLibertyServerException;
    
    /**
     * Checks if the Liberty JVM server contains any FFDCs.
     * 
     * @param sinceCheckpoint
     *            When set to <code>true</code> FFDCs created before the last
     *            checkpoint are not included in the check.
     * @return <code>true</code> if FFDCs are found (of if new FFDCs have been
     *         produced if <code>sinceCheckpoint</code> is <code>true</code>).
     * @throws ZosLibertyServerException
     */
    public boolean containsFFDCs(boolean sinceCheckpoint) throws ZosLibertyServerException;

    /**
     * Save all trace files in logs directory
     */
    public void saveLibertyTraceFiles();

    /**
     * Get the path of the WLP Server Output Directory
     * 
     * @return - the path of the WLP Server Output Directory
     */
    public String getWlpServerDir();
    
    /**
     * @return The Liberty keystore file. <code>null</code> if the file cannot be
     *         found.
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getKeystoreFile() throws ZosLibertyServerException;
    

    
    // TODO From IWLPServer

    /**
     * Return the Liberty JVM server's server.xml directory (absolute path)
     * server.xml is in the Liberty JVM server user directory structure.
     * 
     * @return absolute path to directory containing server.xml
     */
    public String getServerXmlDir();

    /**
     * Get a Liberty XML file from USS
     * 
     * @param xmlDirName - XML document
     * @param xmlFileName -  XML file name     * 
     * @return {@link Document}
     * @throws ZosLibertyServerException
     */
    public Document readXmlFromUSS(String xmlDirName, String xmlFileName) throws ZosLibertyServerException;

    /**
     * Get the Liberty server.xml from USS
     * 
     * @return {@link Document}
     * @throws ZosLibertyServerException
     */
    public Document readServerXmlFromUSS() throws ZosLibertyServerException;

    /**
     * Write a Liberty XML file to USS
     * 
     * @param xmlDoc - XML Document
     * @param xmlDirName - XML directory
     * @param xmlFileName -  XML file name
     * @throws ZosLibertyServerException
     */
    public void writeXmlToUSS(Document xmlDoc, String xmlDirName, String xmlFileName) throws ZosLibertyServerException;
    
    /**
     * Write the Liberty server.xml to USS
     * 
     * @throws ZosLibertyServerException
     */
    public void writeServerXmlToUSS() throws ZosLibertyServerException;

    /**
     * Write the Liberty jvm.options to USS
     * @param options 
     * 
     * @throws ZosLibertyServerException
     */
    public void writeJvmOptionsUSS(String options) throws ZosLibertyServerException;

    /**
     * Deploy a web application to a dropins directory
     * 
     * @param appFileName - the name of the application file
     * @param artifactClass - The class used to locate the artifact
     * @throws ZosLibertyServerException 
     */
    public void deployAppToDropins(String appFileName, Class<?> artifactClass) throws ZosLibertyServerException;

    /**
     * Deploy a web application to a dropins directory
     * 
     * @param appInputStream - An inputstream containing the application file to be deployed
     * @param appFileName - the name of the application file
     * @throws ZosLibertyServerException 
     */
    public void deployAppToDropins(InputStream appInputStream, String appFileName) throws ZosLibertyServerException;

    /**
     * Deploy an EBA application to WLP_OUTPUT_DIR/apps directory
     * 
     * @param appFileName - the name of the application file
     * @param artifactClass - The class used to locate the artifact
     * @throws ZosLibertyServerException 
     */
    public void deployAppToApps(String appFileName, Class<?> artifactClass) throws ZosLibertyServerException;

    /**
     * Deploy an EBA application to WLP_OUTPUT_DIR/apps directory
     * 
     * @param appInputStream - An inputstream containing the application file to be deployed
     * @param appFileName - the name of the application file
     * @throws ZosLibertyServerException 
     */
    public void deployAppToApps(InputStream appInputStream, String appFileName) throws ZosLibertyServerException;
    
    /**
     * Add application entry into server.xml
     * 
     * @param id - id of application to be added
     * @param name - name of application to be added
     * @param location - location of application to be added.
     * If application file is deployed to WLP_OUTPUT_DIR/apps directory
     * then location is the file name, otherwise it is the fully-qualified path.
     * @param type - type of application to be added (for example: "eba") 
     * @throws ZosLibertyServerException 
     */
    public void addApplicationServerXML(String id, String name, String location, String type) throws ZosLibertyServerException;
        
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
     * Delete the entire WLP from USS
     * 
     * @throws ZosLibertyServerException 
     */
    public void removeServerFilesFromUSS() throws ZosLibertyServerException;
    
    /**
     * Get the location of Liberty JVM server's workarea directory
     * workarea directory in the Liberty JVM server output directory structure.
     * 
     * @return absolute location of Liberty JVM server's workarea directory
     * @throws ZosLibertyServerException
     */
    public String getWlpServerWorkareaDir() throws ZosLibertyServerException;
    
    /**
     * Get the location of Liberty JVM server's logs directory
     * logs directory in the Liberty JVM server output directory structure.
     * 
     * @return absolute location of Liberty JVM server's logs directory
     * @throws ZosLibertyServerException
     */
    public String getWlpServerLogsDir() throws ZosLibertyServerException;
    
    /**
     * Get the contents of Liberty JVM server's messages.log
     * messages.log in the Liberty JVM server output directory structure logs directory.
     * 
     * @param binary - get the contents of the file as binary 
     * @return contents of Liberty JVM server's messages.log
     * @throws ZosLibertyServerException
     */
    public OutputStream getMessagesLog(boolean binary) throws ZosLibertyServerException;
    
    /**
     * Get the contents of Liberty JVM server's messages.log
     * messages.log in the Liberty JVM server output directory structure logs directory.
     * 
     * @return contents of Liberty JVM server's messages.log
     * @throws ZosLibertyServerException
     */
    public String getMessagesLogString() throws ZosLibertyServerException;
    
    /**
     * Get the fullname of the messages.log uss file.
     * 
     * @return String of the messages.log
     * @throws ZosLibertyServerException
     */
    public String getMessagesLogName() throws ZosLibertyServerException;
    
    /**
     * Search Liberty JVM server's messages.log for specified text
     * 
     * @param text
     * @param binary
     * @return true if the text message was found in the log, false otherwise
     * @throws ZosLibertyServerException
     */
    public boolean searchMessagesLogForText(String text, boolean binary) throws ZosLibertyServerException;
    
    /**
     * Waits until a text string (regex) appears in the messages.log.  Will timeout using the 
     * default resource timeout value
     * 
     * @param text - regex string
     * @return whether the text was found in time
     * @throws ZosLibertyServerException
     */
    public boolean waitForMessagesLogText(String text) throws ZosLibertyServerException;
    
    /**
     * Waits for message CWWKF0011I to appear in the log
     * 
     * @return whether the message was found in time
     * @throws ZosLibertyServerException
     */
    public boolean waitForLibertyStart() throws ZosLibertyServerException;
    
    /**
     * Get the contents of the Liberty servers logs directory
     * @return the Liberty server logs
     * @throws ZosLibertyServerException
     */
    public IZosLibertyServerLogs getLogs() throws ZosLibertyServerException ;
    
    /**
     * Use the Liberty securityUtility command to encode a password
     * @param password - The password to encode
     * @return - The encoded password
     * @throws ZosLibertyServerException 
     * @throws CICSManagerException 
     */
    public String securityUtilityEncode(String password) throws ZosLibertyServerException;
        
    /**
     * Use the Liberty securityUtility command to encode a password
     * @param password - The password to encode
     * @param javaHome - Specify the java home to be used by the command
     * @return - The encoded password
     * @throws ZosLibertyServerException 
     * @throws CICSManagerException 
     */
    public String securityUtilityEncode(String password, String javaHome) throws ZosLibertyServerException;
    /**
     * Use the Liberty securityUtility to create a new keyStore
     * @param cics <p>The CICS region that this WLP is inside (only way we can get applid and temp directory from here)</p>
     * @param password <p>The password to create the certificate/keystore with
     * @return <p>Full path to the new keystore file</p>
     * @throws ZosLibertyServerException
     */
    public String securityGenerateKeystore(/*ICICS cics,*/ String password) throws ZosLibertyServerException;
    
    /**
     * Adds the a bundle Repository to the Liberty server
     * @param dir
     * @param includes
     * @throws ZosLibertyServerException
     */
    public void addBundleToRepository(String dir, String includes) throws ZosLibertyServerException;
    
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
