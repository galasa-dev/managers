/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

import java.util.Map;

import org.w3c.dom.Document;

import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosliberty.IZosLibertyServer;

/**
 * Represents a CICS JVM server JVM profile
 */
public interface IJvmprofile {
	
	/**
	 * Returns the JVM profile as a {@link IZosUNIXFile} object
	 * @return the JVM profile
	 */
	public IZosUNIXFile getProfile();
	
	/**
	 * Sets JVM server option or JVM system property in the JVM profile of the format <code>key=value</code>.<br>
	 * If the key exists, it will be replaced. To append a value to an existing option or property, use {@link #appendProfileValue(String, String)}<p> 
	 * Examples:<br>
	 * <table border="1">
	 *   <tr>
	 *     <th style="width:25%">Option</th>
	 *     <th style="width:25%">key</th>
	 *     <th style="width:25%">value</th>
	 *   </tr>
	 *   <tr>
	 *     <td><code>PRINT_JVM_OPTIONS=TRUE</code></td>
	 *     <td><code>PRINT_JVM_OPTIONS</code></td>
	 *     <td><code>TRUE</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Dcom.ibm.cics.jvmserver.trace.format=FULL</code></td>
	 *     <td><code>-Dcom.ibm.cics.jvmserver.trace.format</code></td>
	 *     <td><code>FULL</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Xms256M</code></td>
	 *     <td><code>-Xms</code></td>
	 *     <td><code>256M</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Xshareclasses:printStats</code></td>
	 *     <td><code>-Xshareclasses</code></td>
	 *     <td><code>:printStats</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Xnocompressedrefs</code></td>
	 *     <td><code>-Xnocompressedrefs</code></td>
	 *     <td><code>null</code></td>
	 *   </tr>
	 * </table>
	 * <p>
	 * @param key the JVM server option or JVM system property key
	 * @param value the JVM server option or JVM system property value
	 */
	public void setProfileValue(String key, String value);	
	
	/**
	 * Append a value to a JVM server option or JVM system property in the JVM profile of the format <code>key=value</code> in the JVM profile. 
	 * For example, given an existing JVM profile option of:<br>
	 * <code>OSGI_BUNDLES=/tmp/bundle1.jar</code><br> 
	 * the method call <code>appendProfileValue("OSGI_BUNDLES", "/tmp/bundle2.jar")</code><br> would set the option to:<br>
	 * <code>OSGI_BUNDLES=/tmp/bundle1.jar,\\\n/tmp/bundle2.jar</code>
	 * <p>
	 * If the key does not exist, then this method performs the same function as {@link #setProfileValue(String, String)}	 * 
	 * @param key the JVM server option or JVM system property key
	 * @param value the JVM server option or JVM system property value
	 */
	public void appendProfileValue(String key, String value);
	
	/**
	 * Returns the value of a JVM server option or JVM system property in the JVM profile
	 * @param key the JVM server option or JVM system property key
	 * @return the JVM server option or JVM system property value
	 */
	public String getProfileValue(String key);	
	
	/**
	 * Removes the JVM server option or JVM system property from the JVM profile
	 * @param key
	 */
	public void removeProfileValue(String key);
	
	/**
	 * Returns a {@link Map} of the JVM server options and JVM system property in the JVM profile
	 * @return content of the JVM Profile
	 */
	public Map<String, String> getProfileMap();
	
	/**
	 * Build the JVM profile on the zOS UNIX file system
	 * 
	 * @throws CicsResourceException
	 */
	public void build() throws CicsJvmserverResourceException;

	/**
	 * Add a previously created JVM profile include file to the JVM profile using the <code>%INCLUDE</code> option
	 * @param profileInclude the file to include
	 */
	public void addProfileIncludeFile(IZosUNIXFile profileInclude);

	/**
	 * Creates a new JVM profile include file and adds it to the JVM profile using the <code>%INCLUDE</code> option.<br>
	 * See {@link #setProfileValue(String, String)} for the format of key/value pairs 
	 * @param name the file name for new JVM profile include file
	 * @param content the content for the new JVM profile include file
	 *  
	 * @return a {@link IZosUNIXFile} object representing the new JVM profile include file
	 * @throws CicsJvmserverResourceException
	 */
	public IZosUNIXFile addProfileIncludeFile(String name, Map<String, String> content) throws CicsJvmserverResourceException;

	/**
	 * Removes and deletes a JVM profile include file from the JVM profile 
	 * @param name the JVM profile include file to remove
	 */
	public void removeProfileIncludeFile(IZosUNIXFile name) throws CicsJvmserverResourceException;

	/**
	 * Removes and deletes all JVM profile included files from the JVM profile
	 */
	public void removeAllProfileIncludes() throws CicsJvmserverResourceException;	/**
	 * Enable DB2 JCC trace by adding the jcc trace properties to the JVM profile 
	 * @throws CicsJvmserverResourceException 
	 */
	public void addJCCTraceProperties() throws CicsJvmserverResourceException;	
	
	/**
	 * Returns a HashMap of jcc trace properties
	 * @return the jcc properties
	 * @throws CicsJvmserverResourceException 
	 */
	public Map<String, String> getJCCTraceProperties() throws CicsJvmserverResourceException;	
	
	/**
	 * Save the DB2 JCC trace files to the default location in the Results Archive Store
	 * @throws CicsJvmserverResourceException
	 */
	public void saveJCCTraceFiles() throws CicsJvmserverResourceException;	
	
	/**
	 * Save the DB2 JCC trace files to the Results in the Archive Store
	 * @param rasPath path in Results Archive Store
	 * @throws CicsJvmserverResourceException
	 */
	public void saveJCCTraceFiles(String rasPath) throws CicsJvmserverResourceException;

	/**
	 * Convenience method that adds remote debug properties to the JVM profile for debug of local runs
	 * @param debugPort port for debug
	 * @param suspend suspend the JVM until the remote debugger is connected
	 * @throws EnvironmentException 
	 */
	public void addRemoteDebug(int debugPort, boolean suspend) throws CicsJvmserverResourceException;
	
	/**
	 * Sets the zOS Liberty server object associated with this JVM server
	 * @param zosLibertyServer the {@link IZosLibertyServer} object to associate with this JVM server
	 */
	public void setLibertyServer(IZosLibertyServer zosLibertyServer);

	/**
	 * Returns the zOS Liberty server object associated with this JVM server
	 * @return the {@link IZosLibertyServer} associated with this JVM server
	 */
	public IZosLibertyServer getLibertyServer();

	/**
	 * Set the value of the <code>WLP_INSTALL_DIR</code> environment variable in the JVM profile<br>Galasa sets the default value of<code>&USSHOME;/wlp</code>
	 * @param wlpInstallDir the value of <code>WLP_INSTALL_DIR</code>
	 * @throws CicsJvmserverResourceException
	 */
	public void setWlpInstallDir(String wlpInstallDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>WLP_USER_DIR</code> environment variable in the JVM profile
	 * @param wlpUserDir the value of <code>WLP_USER_DIR</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpUserDir(String wlpUserDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>WLP_OUTPUT_DIR</code> environment variable in the JVM profile
	 * @param wlpOutputDir the value for <code>WLP_OUTPUT_DIR</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpOutputDir(String wlpOutputDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>ZCEE_INSTALL_DIR</code> environment variable in the JVM profile using the value supplied
	 * in the Galasa Configuration Property Service
	 * @param zOSConnectInstallDir the value of <code>ZCEE_INSTALL_DIR</code>
	 * @throws CicsJvmserverResourceException
	 */
	void setZosConnectInstallDir() throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>ZCEE_INSTALL_DIR</code> environment variable in the JVM profile
	 * @param zOSConnectInstallDir the value of <code>ZCEE_INSTALL_DIR</code>
	 * @throws CicsJvmserverResourceException
	 */
	public void setZosConnectInstallDir(String zOSConnectInstallDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>com.ibm.cics.jvmserver.wlp.server.name</code> JVM system property in the JVM profile.
	 * Liberty defaults this to defaultServer. 
	 * @param serverName the value for <code>com.ibm.cics.jvmserver.wlp.server.name</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerName(String serverName) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>com.ibm.cics.jvmserver.wlp.autoconfigure</code> JVM system property in the JVM profile
	 * @param autoconfigure the value for <code>com.ibm.cics.jvmserver.wlp.autoconfigure</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpAutoconfigure(boolean autoconfigure) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>com.ibm.cics.jvmserver.wlp.server.host</code> JVM system property in the JVM profile
	 * @param hostname the value of<code>com.ibm.cics.jvmserver.wlp.server.host</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerHost(String hostname) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>com.ibm.cics.jvmserver.wlp.server.http.port</code> JVM system property in the JVM profile
	 * @param httpPort the value of<code>com.ibm.cics.jvmserver.wlp.server.http.port</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerHttpPort(int httpPort) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>com.ibm.cics.jvmserver.wlp.server.https.port</code> JVM system property in the JVM profile
	 * @param httpsPort the value of<code>com.ibm.cics.jvmserver.wlp.server.https.port</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerHttpsPort(int httpPort) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>com.ibm.cics.jvmserver.wlp.wab</code> JVM system property in the JVM profile
	 * @param wabEnabled the value of<code>com.ibm.cics.jvmserver.wlp.wab</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerWabEnabled(boolean wabEnabled) throws CicsJvmserverResourceException;

	/**
	 * Returns the value of the <code>WLP_INSTALL_DIR</code> environment variable in the JVM profile 
	 * @return the value of <code>WLP_INSTALL_DIR</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public String getWlpInstallDir() throws CicsJvmserverResourceException;

	/**
	 * Returns the value of the <code>WLP_USER_DIR</code> environment variable in the JVM profile
	 * @return the value of <code>WLP_USER_DIR</code>
	 */
	public String getWlpUserDir();
	
	/**
	 * Returns the value of the <code>WLP_OUTPUT_DIR</code> environment variable in the JVM profile
	 * @return the value of <code>WLP_OUTPUT_DIR</code>
	 */
	public String getWlpOutputDir();
	
	/**
	 * Returns the value of the <code>com.ibm.cics.jvmserver.wlp.server.name</code> JVM system property in the JVM profile
	 * @return the value of <code>com.ibm.cics.jvmserver.wlp.server.name</code>
	 */
	public String getWlpServerName();	
	
	/**
	 * Returns the value of the <code>com.ibm.cics.jvmserver.wlp.autoconfigure</code> JVM system property in the JVM profile
	 * @return the value of <code>com.ibm.cics.jvmserver.wlp.autoconfigure</code>
	 */
	public String getWlpAutoconfigure();

	/** 
	 * Returns the value of the <code>com.ibm.cics.jvmserver.wlp.server.host</code> JVM system property in the JVM profile
	 * @return the value of <code>com.ibm.cics.jvmserver.wlp.server.host</code>
	 */	
	public String getWlpServerHost();
	
	/**
	 * Returns the value of the <code>com.ibm.cics.jvmserver.wlp.server.http.port</code> JVM system property in the JVM profile
	 * @return the value of <code>com.ibm.cics.jvmserver.wlp.server.http.port</code>
	 */
	public String getWlpServerHttpPort();

	/**
	 * Returns the value of the <code>com.ibm.cics.jvmserver.wlp.server.https.port</code> JVM system property in the JVM profile
	 * @return the value of <code>com.ibm.cics.jvmserver.wlp.server.https.port</code>
	 */
	public String getWlpServerHttpsPort();	

	/**
	 * Returns the value of the <code>com.ibm.cics.jvmserver.wlp.wab</code> JVM system property in the JVM profile
	 * @return the value of <code>com.ibm.cics.jvmserver.wlp.wab</code>
	 */
	public boolean getWlpServerWabEnabled();

	/**
	 * Add a previously created JVM profile include file to the JVM profile using the <code>LIBERTY_INCLUDE_XML</code> option
	 * @param profileInclude the file to include
	 */
	public void addLibertyIncludeXml(IZosUNIXFile profileInclude);

	/**
	 * Creates a new Liberty server.xml include file and adds it to the JVM profile using the <code>LIBERTY_INCLUDE_XML</code>
	 * option using {@link String} content. The content will NOT be parsed for invalid XML
	 * @param name the file name for new Liberty server.xml include file
	 * @param content the content for the new Liberty server.xml include file
	 *  
	 * @return a {@link IZosUNIXFile} object representing the new Liberty server.xml include file
	 * @throws CicsJvmserverResourceException
	 */
	public IZosUNIXFile addLibertyIncludeXml(String name, String content) throws CicsJvmserverResourceException;

	/**
	 * Creates a new Liberty server.xml include file and adds it to the JVM profile using the <code>LIBERTY_INCLUDE_XML</code>
	 * option using {@link Document} content
	 * @param name the file name for new Liberty server.xml include file
	 * @param content the content for the new Liberty server.xml include file
	 *  
	 * @return a {@link IZosUNIXFile} object representing the new Liberty server.xml include file
	 * @throws CicsJvmserverResourceException
	 */
	public IZosUNIXFile addLibertyIncludeXml(String name, Document content) throws CicsJvmserverResourceException;

	/**
	 * Removes and deletes a JVM profile include file from the JVM profile 
	 * @param name the JVM profile include file to remove
	 */
	public void removeLibertyIncludeXml(IZosUNIXFile name) throws CicsJvmserverResourceException;

	/**
	 * Removes and deletes all JVM profile included files from the JVM profile
	 */
	public void removeAllLibertyIncludeXmls() throws CicsJvmserverResourceException;
}
