/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Represents the server.xml configuration file in a zOS Liberty Server
 */
public interface IZosLibertyServerXml {

	/**
	 * Replace the Liberty server.xml from {@link Document} content
	 * @param content the server.xml content
	 */
	public void setServerXmlFromDocument(Document content);

	/**
	 * Replace the Liberty server.xml from {@link String} content
	 * @param content the server.xml content
	 * @throws ZosLibertyServerException 
	 */
	public void setServerXmlFromString(String content) throws ZosLibertyServerException;

	/**
	 * Get the Liberty server.xml as a {@link Document}
	 * @return the content of the Liberty server.xml
	 */
	public Document getServerXmlAsDocument();
	
	/**
	 * Create the server.xml file on the zOS UNIX file system
	 * @throws ZosLibertyServerException
	 */
	public void build() throws ZosLibertyServerException;
	
	/**
	 * Add a simple XML element to the Liberty serverXml Document object
	 * 
	 * @param elementName the XML element name
	 * @param elementId the XML element id
	 * @param elementAttributes the XML element attributes
	 * @throws ZosLibertyServerException
	 */
	public void addXmlElement(String elementName, String elementId, Map<String, String> elementAttributes) throws ZosLibertyServerException;

	/**
	 * Update the content of the {@link IZosLibertyServerXml) with the content of the server.xml file from the zOS UNIX file system 
	 * @throws ZosLibertyServerException
	 */
	public void loadFromFileSystem() throws ZosLibertyServerException;
	
	// TODO from IWLPServer
	
	
	
	
	
	
	
	public IServerXmlElement newServerXmlElement(String elementName, Map<String, String> elementAttributes);
	
	
	public interface IServerXmlElement {
		
	}

	/**
	 * Enables dropins for this Liberty server by setting the value of property <code>dropinsEnabled</code> in the <code>applicationMonitor</code> element
	 * @throws ZosLibertyServerException
	 */
	public void setDropinsEnabled(boolean enabled) throws ZosLibertyServerException;

	/** 
	 * Adds an application tag to a Liberty server's 'server.xml'
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
	 * Set or update the properties of the <code>httpEndpoint</code> with the <code>id="defaultHttpEndpoint"</code>
	 * @param host the value of the HTTP hostname
	 * @param httpPort the value of the HTTP port
	 * @param httpsPort the value of the HTTPS port
	 * @throws ZosLibertyServerException
	 */
	public void updateDefaultHttpEndpoint(String host, String httpPort, String httpsPort) throws ZosLibertyServerException;
	
	
	
	
	
	
	
	/**
	 * Add a compound XML element to the Liberty serverXml Document object
	 * 
	 * @param parent - The parent element
	 * @param elementName - The top element name
	 * @param elementId - The id of the top element
	 * @param elementAttributes - The attributes of the top element
	 * @param subElements - An {@link IxmlElementList} of dataSource sub-elements
	 * @throws ZosLibertyServerException
	 */
	public void addCompoundXmlElement(String parent, String elementName, String elementId, Map<String, String> elementAttributes, IxmlElementList subElements) throws ZosLibertyServerException;
	
	/**
	 * Add a compound element to the Liberty serverXml Document object
	 * 
	 * @param parent - The parent element
	 * @param parentId - The id of the parent element
	 * @param elementName - The top element name
	 * @param elementId - The id of the top element
	 * @param elementAttributes - The attributes of the top element
	 * @param subElements - An {@link IxmlElementList} of dataSource sub-elements
	 * @throws ZosLibertyServerException
	 */
	public void addCompoundXmlElementById(String parent, String parentId, String elementName, String elementId, Map<String, String> elementAttributes, IxmlElementList subElements) throws ZosLibertyServerException;
	
	/**
	 * Add a dataSource to the Liberty serverXml Document object 
	 *
	 * @param id - The id of the dataSource
	 * @param jndiName - The dataSource JNDI name
	 * @param dataSourceAttributes - Further attributes for the dataSource or null
	 * @param dataSourceSubElements - An {@link IxmlElementList} of dataSource sub-elements. of dataSource sub-elements.  
	 * e.g.
	 * <pre>
	 * String dataSourceId = "dataSource1";
	 * String dataSourceJndiName = "jdbc/dataSource1";
	 * String libraryId = "db2Lib";
	 * HashMap<String, String> dataSourceAttributes = new HashMap<String, String>();
	 * dataSourceAttributes.put("type", "javax.sql.DataSource");
	 * 
	 * HashMap<String, HashMap<String, String>> dataSourceSubElements = new HashMap<String, HashMap<String, String>>();
	 * HashMap<String, String> jdbcDriver = new HashMap<String, String>();
	 * jdbcDriver.put("libraryRef", libraryId);
	 * dataSourceSubElements.put("jdbcDriver", jdbcDriver);
	 * wlpServer.addDataSource(dataSourceId, dataSourceJndiName, dataSourceAttributes, dataSourceSubElements);</pre>
	 * produces<pre>
	 * &lt;dataSource id="dataSource1" type="javax.sql.DataSource"&gt;
	 *     &lt;jdbcDriver libraryRef="db2Lib"/&gt;
	 * &lt;/dataSource&gt;</pre>
	 * Can be null for no sub-elements
	 * @throws ZosLibertyServerException 
	 */
	public void addDataSource(String id, String jndiName, Map<String, String> dataSourceAttributes, IxmlElementList dataSourceSubElements) throws ZosLibertyServerException;
	
	/**
	 * Delete a dataSource from the Liberty serverXml Document object.
	 *
	 * @param id - The id of the dataSource
	 * @throws ZosLibertyServerException 
	 */
	public void deleteDataSource(String id) throws ZosLibertyServerException;
	
	/**
	 * Add a CICS dataSource to the Liberty serverXml Document object
	 * 
	 * @param id - The id of the dataSource
	 * @param jndiName - The dataSource JNDI name
	 * @param dataSourceAttributes - Further attributes for the dataSource or null
	 * @param dataSourceSubElements - An {@link IxmlElementList} of dataSource sub-elements.  
	 * @throws ZosLibertyServerException
	 */
	public void addCICSDataSource(String id, String jndiName, Map<String, String> dataSourceAttributes, IxmlElementList dataSourceSubElements) throws ZosLibertyServerException;
	
	/**
	 * Delete a CICS dataSource from the Liberty serverXml Document object.
	 *
	 * @param id - The id of the dataSource
	 * @throws ZosLibertyServerException 
	 */
	public void deleteCICSDataSource(String id) throws ZosLibertyServerException;
	
	/**
	 * Add a library to the Liberty serverXml Document object
	 * 
	 * @param libraryId - The id of the library
	 * @param libraryAttibutes - The attributes of the library or null
	 * @param librarySubElements - An {@link IxmlElementList} of library sub-elements.
	 * @throws ZosLibertyServerException
	 */
	public void addLibrary(String libraryId, Map<String, String> libraryAttibutes, IxmlElementList librarySubElements) throws ZosLibertyServerException;
	
	/**
	 * Delete a Library from the Liberty serverXml Document object
	 * 
	 * @param id - The id of the library
	 * @throws ZosLibertyServerException
	 */
	public void deleteLibrary(String id) throws ZosLibertyServerException;
	
	
	/**
	 * Delete a node with a specific value from an XML document
	 * 
	 * @param xmlDoc - The XML document 
	 * @param xmlTag - The tag to delete
	 * @param xmlItem - The item to delete
	 * @param value - The value of the item
	 */
	public void deleteXmlNode(Document xmlDoc, String xmlTag, String xmlItem, String value);
	
	/**
	 * Delete a node from an XML document
	 * 
	 * @param xmlDoc - The XML document 
	 * @param xmlTag - The tag to delete
	 */
	public void deleteXmlNode(Document xmlDoc, String xmlTag);
	
	/**
	 * Delete a node from an XML document with specific id
	 * 
	 * @param xmlDoc - The XML document 
	 * @param xmlTag - The tag to delete
	 * @param id - The id of the node
	 */
	public void deleteXmlNodeById(Document xmlDoc, String xmlTag, String id);
	
	/**
	 * Add a node to an XML document under a parent identified by tag
	 * 
	 * @param xmlDoc - The XML document 
	 * @param xmlTag - The XML tag to add
	 * @param parent - The parent tag
	 * @param nodeAttributes - The node attributes
	 */
	public void addXmlNode(Document xmlDoc, String xmlTag, String parent, Map<String, String> nodeAttributes); 
	
	/**
	 * Add a node to an XML document under a parent identified by tag
	 * 
	 * This method is added by Amily (Chen BJ Jiang).
	 * As we know there will be some nodes with the same tag but different ID, if we want add a node to this kind
	 * of node, the method "addXmlNode" can't help us to do that. So I add a method "addXmlNodeByItemNo", you can 
	 * use this method to add child node to the parent node with the itemNo.
	 * 
	 * @param xmlDoc - The XML document 
	 * @param xmlTag - The XML tag to add
	 * @param parent - The parent tag
	 * @param nodeAttributes - The node attributes
	 * @param itemNo - The itemNo of the parent
	 */
	public void addXmlNodeByItemNo(Document xmlDoc, String xmlTag, String parent, Map<String, String> nodeAttributes, int itemNo); 
	
	/**
	 * Add a node to an XML document with under a parent identified by id
	 * 
	 * @param xmlDoc - The XML document 
	 * @param xmlTag - The XML tag to add
	 * @param parent - The parent tag
	 * @param parentId - The parent id
	 * @param nodeAttributes - The node attributes
	 */
	public void addXmlNodeById(Document xmlDoc, String xmlTag, String parent, String parentId, Map<String, String> nodeAttributes);
	
	/**
	 * Print contents of XML Document
	 * 
	 * @param xmlDoc - XML Document
	 * @return String representation of server.xml
	 * @throws ZosLibertyServerException
	 */
	public String printXmlDoc(Document xmlDoc) throws ZosLibertyServerException;
	
	/**
	 * Print contents of server.xml Document
	 * 
	 * @return String representation of server.xml
	 * @throws ZosLibertyServerException
	 */
	public String printServerXmlDoc() throws ZosLibertyServerException;
	
	/**
	 * Print contents of server.xml on USS
	 * 
	 * @return String representation of server.xml
	 * @throws ZosLibertyServerException
	 */
	public String printServerXmlFromUss() throws ZosLibertyServerException;
	
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
	 * Get a node from an XML document
	 * 
	 * @param xmlDoc - The XML document 
	 * @param xmlTag - The XML tag to get
	 * @param nodeId - Optional, the value of the id attribute of the node to get.
	 * Set to null to return the first node matching the xmlTag.
	 * @return org.w3c.dom.Node from the XML document matching the XML tag
	 * and optionally id attribute value specified (null if not found).
	 */
	public Node getXmlNode(Document xmlDoc, String xmlTag, String nodeId);
	
	/**
	 * Get host value from defaultHttpEndpoint element in server.xml
	 * 
	 * @param serverXML - server.xml
	 * @return host value as a String (null if not found)
	 */
	public String getHttpEndpointHost(Document serverXML);

	/**
	 * Get httpPort value from defaultHttpEndpoint element in server.xml
	 * 
	 * @param serverXML - server.xml
	 * @return httpPort value as a String (null if not found)
	 */
	public String getHttpEndpointHttpPort(Document serverXML);
	/**
	 * Get httpsPort value from defaultHttpEndpoint element in server.xml
	 * 
	 * @param serverXML - server.xml
	 * @return httpsPort value as a String (null if not found)
	 */
	public String getHttpEndpointHttpsPort(Document serverXML);
		
	/**
	 * Adds one or more features to the featureManager element in server.xml
	 * @param features - one or more features to be added (including version number).
	 * @throws ZosLibertyServerException 
	 * @throws DOMException 
	 */
	public void addFeatureServerXML(String... features) throws ZosLibertyServerException;
	
	/**
	 * Removes the feature with featureName from server.xml if present
	 * 
	 * @param featureName <p>The name of the feature to try and remove from the config</p>
	 * @return <p>True if a feature with featureName was found and removed or false if no
	 *  feature with that name was found ergo no change was made to the config.</p>
	 * @throws ZosLibertyServerException
	 * @throws DOMException
	 */
	public boolean removeFeatureServerXML(String featureName) throws ZosLibertyServerException;
	
	/**
	 * Get list of enabled features from featureManager element in server.xml
	 * 
	 * @param serverXML - server.xml
	 * @return ArrayList<String> of enabled features (null if none found) 
	 * @throws ZosLibertyServerException 
	 */
	public ArrayList<String> getEnabledFeatures(Document serverXML) throws ZosLibertyServerException;
	
	/**
	 * Check whether a specific feature is enabled in
	 * featureManager element in server.xml
	 * 
	 * @param featureName - Name of feature to check (use either full feature name
	 * eg. "jsp-2.2" or partial name eg. "jsp" if version is not significant). 
	 * @param serverXML - server.xml
	 * @return whether the feature is enabled
	 * @throws ZosLibertyServerException 
	 */
	public boolean isFeatureEnabled(String featureName, Document serverXML) throws ZosLibertyServerException;
	
	/**
	 * 
	 * An IxmlElementList contains a list of XML elements
	 *
	 */
	public interface IxmlElementList 
	{    
		/**
	     * Put an XML element in the list
	     * 
	     * @param elementName - The name of the element to add
	     * @param attributes - A HashMap<String, String> of attributes
	     */
		public void add(String elementName, HashMap<String, String> attributes);

		/**
		 * Returns the length of the list
		 * 
		 * @return - The length
		 */
		public int getLength();
		
		/**
		 * Get an XML element in the list
		 * @param index - An index in the list
		 * @return - The element
		 */
		public String getElement(int index);
		
		/**
		 * Get an XML element attributes in the list
		 * @param index - An index in the list
		 * @return - The attributes
		 */
		public HashMap<String, String> getAttributes(int index);

		/**
		 * Clear the XML element list
		 */
	    public void clear();
	}


	/**
	 * Create an {@link IxmlElementList} 
	 * @return {@link IxmlElementList}
	 */
	public IxmlElementList createIxmlElementList();

	/**
	 * Adds the a bundle Repository to the Liberty server
	 * @param dir
	 * @param includes
	 * @throws ZosLibertyServerException
	 */
	public void addBundleToRepository(String dir, String includes) throws ZosLibertyServerException;

	
	/**
	 * Removes all occurrences of one or more (comma separated) elements from Server.xml
	 * @param elements one or more elements to remove all occurrences of
	 */
	public void removeElements(String... elements);


	/**
	 * Adds a basicRegistry with user provided to server.xml
	 * @param userid the userid 
	 * @param password 
	 * @return the basic registry element
	 * 
	 */
	public Element addBasicRegistry(String userid, String password);
	
	/**
	 * Adds a basicRegistry with user and its' group provided to server.xml
	 * @param userid the userid 
	 * @param password 
	 * @param group 
	 * @return the basic registry element
	 * 
	 */
	public Element addBasicRegistry(String userid, String password, String group);


	/**
	 * Adds a SAF registry configuration to server.xml
	 * @return the SAF registry element
	 */
	public Element addSafRegistry();
	
	
	/**
	 * Adds a safCredentials element to server.xml
	 * @param profilePrefix
	 * @param unauthenticatedUser
	 * @param mapDistributedIdentities
	 * @return the safCredentials element
	 */
	public Element addSafCredentials(String profilePrefix, String unauthenticatedUser, boolean mapDistributedIdentities);
	
	
	
	/**
	 * @param profilePrefix
	 * @param mapDistributedIdentities
	 * @return the safCredentials element
	 */
	public Element addSafCredentials(String profilePrefix, boolean mapDistributedIdentities);	
	
	/**
	 * Using this method intentionally omits the mapDistributedIdentities value, 
	 * which allows it to default or be set elsewhere.
	 * @param profilePrefix
	 * @return the safCredentials element
	 */
	public Element addSafCredentials(String profilePrefix);		
		
	/**
	 * Adds a default SafRoleMapper element for the applid.rolepattern provided
	 * @param applid
	 * @param rolePattern
	 * @param toUpperCase Convert EJBROLE resource profile name to upper case.
	 * @return the SafRolemapper element
	 */
	public Element addSafRoleMapper(String applid, String rolePattern, boolean toUpperCase);
	
	
	/**
	 * Adds a default safAuthorization element with id=saf
	 * @return the safAuthorization element
	 */
	public Element addSafAuthorization();


	/**
	 * Adds a default OpenID Connect Provider using a local store
	 * with a pre-configured 'default' client/secret with 
	 * corresponding URLs.
	 * 
	 * @param rpOrigin the Origin of the RP (scheme://hostname:port)
	 * @param specialSubjectType 
	 * @return the OIDCProvider element with a standard local RP client
	 */
	public Element addOIDCProviderLocalStore(URL rpOrigin, String specialSubjectType);
	
	/**
	 * Adds a default OpenID Connect Provider using a local store
	 * with a pre-configured 'default' client/secret with 
	 * corresponding URLs.
	 * Optional add the 'issuerIdentifier' setting 
	 * 
	 * @param rpOrigin the Origin of the RP (scheme://hostname:port)
	 * @param issuerIdentifier 
	 * @param specialSubjectType 
	 * @return the OIDCProvider element with a standard local RP client
	 */
	public Element addOIDCProviderLocalStore(URL rpOrigin, String issuerIdentifier, String specialSubjectType);


	/**
	 * Adds a default OpenID Connect client setup to server.xml
	 * This includes the URL to the corresponding provider(OP)
	 * and a matching client/secret setup.
	 * Optional add the 'mapIdentityToUserRegistry' setting to
	 * insist on a match in the RPs registry with the authenticated identity
	 * from the OP.
	 * 	
	 * @param opOrigin the Origin of the OP (scheme://hostname:port)
	 * @param mapIdentityToRegistryUser rejects the request if the userIdentity isn't found in the registry
	 * @return the OpenID Connect Client element
	 */
	public Element addOIDCClient(URL opOrigin, boolean mapIdentityToRegistryUser);

	/**
	 * Adds a default OpenID Connect client setup to server.xml
	 * This includes the URL to the corresponding provider(OP)
	 * and a matching client/secret setup.
	 * Optional add the 'mapIdentityToUserRegistry' setting to
	 * insist on a match in the RPs registry with the authenticated identity
	 * from the OP.
	 * Optional add the 'issuerIdentifier' setting 
	 * 	
	 * @param opOrigin the Origin of the OP (scheme://hostname:port)
	 * @param mapIdentityToRegistryUser rejects the request if the userIdentity isn't found in the registry
	 * @param issuerIdentifier 
	 * @return the OpenID Connect Client element
	 */
	public Element addOIDCClient(URL opOrigin, boolean mapIdentityToRegistryUser, String issuerIdentifier);

	/**
	 * Add an Application to server.xml
	 * 
	 * @param type "war, "ear", "eba" 	 
	 * @param appsDir directory in which the Application is found
	 * @param app the name of the application
	 * @return the application element
	 */
	public Element addApplication(String type, String appsDir, String app);


	/**
	 * Adds an application-bnd element to the application element.
	 * The application-bnd maps the specified user into the protected role.
	 * Note: Application-bnd is ignored in favour of EJBroles if
	 * safAuthorization is configured.
	 * 
	 * @param application the element to add the application-bnd to
	 * @param protectingRole the name of the role protecting the application
	 * @param user the user to add into the protecting role
	 * @return application-bnd element
	 */
	public Element addApplicationBndWithUser(Element application, String protectingRole, String user);

	
	/**
	 * Adds an application-bnd element to the application element.
	 * The application-bnd maps the specified group into the protected role.
	 * Note: Application-bnd is ignored in favour of EJBroles if
	 * safAuthorization is configured.
	 * 
	 * @param application the element to add the application-bnd to
	 * @param protectingRole the name of the role protecting the application
	 * @param group the group to add into the protecting role
	 * @return application-bnd element
	 */
	public Element addApplicationBndWithGroup(Element application, String protectingRole, String group);
	
	
	/**
	 * Adds an application-bnd element to the application element.
	 * The application-bnd maps ALL_AUTHENTICATED_USERS into the protected role.
	 * Note: Application-bnd is ignored in favour of EJBroles if
	 * safAuthorization is configured.
	 * 
	 * @param application the element to add the application-bnd to
	 * @param protectingRole the name of the role protecting the application
	 * @param specialSubjectType 
	 * @return application-bnd element
	 */
	public Element addApplicationBndAllAuthenticated(Element application, String protectingRole, String specialSubjectType);


	/**
	 * Adds <authentication cacheEnabled="false"/> to disable the auth-cache.
	 * Useful in testing to ensure each test request is authenticated in its own
	 * right without pollution from previous successful tests.
	 */
	public void disableAuthCache();
	
	
	/**
	 * Adds the <config> element to server.xml which controls the refresh interval,
	 * the onError action, and the updateTrigger.
	 * 
	 * Default values are "WARN", "500ms", "polled".
	 * 
	 * @param onError (WARN, IGNORE, FAIL)
	 * @param monitorInterval value + (h, m, s, ms)  the frequency at which polling occurs
	 * @param updateTrigger (polled, mbean, disabled)
	 * @return the config element
	 */
	public Element addConfig(String onError, String monitorInterval, String updateTrigger);


	/**
	 * Adds the a default implementation of the LDAP registry
	 * @param ldap server reference
	 * @param ldapUser 
	 * @return the LDAP element in server.xml
	 */
	public Element addLdapRegistry(/*ILdapServer ldap, ILdapUser ldapUser*/);
	
	
	/**
	 * Adds a Liberty trace specification element to server.xml
	 * @param traceString - the traceSpecification string required
	 * @return the trace element
	 */
	public Element addTraceSpecification(String traceString);
	
	/**
	 * Adds a Db2 Type4 DataSource element to server.xml
	 * @param filesetClassesDir 
	 * @param filesetClassesIncludes 
	 * @param filesetLibraryDir 
	 * @param db2PropertiesCurrentSchema 
	 * @param db2PropertiesdatabaseName 
	 * @param db2PropertiesPassword 
	 * @param db2PropertiesPortNumber 
	 * @param db2PropertiesServerName 
	 * @param db2PropertiesUser 
	 * 
	 * @return the Db2 Type4 DataSource element
	 */
	public Element addDb2Type4DataSource(String filesetClassesDir, String filesetClassesIncludes, String filesetLibraryDir, String db2PropertiesCurrentSchema, String db2PropertiesdatabaseName, 
			String db2PropertiesPassword, String db2PropertiesPortNumber, String db2PropertiesServerName, String db2PropertiesUser);
	
	/**
	 * Adds a default OpenID Connect Provider using a Database store
	 * @param specialSubjectType 
	 * @param authenticatedGroupid 
	 * @param filesetClassesDir 
	 * @param filesetClassesIncludes 
	 * @param filesetLibraryDir 
	 * @param db2PropertiesCurrentSchema 
	 * @param db2PropertiesdatabaseName 
	 * @param db2PropertiesPassword 
	 * @param db2PropertiesPortNumber 
	 * @param db2PropertiesServerName 
	 * @param db2PropertiesUser 
	 * 
	 * @return the OIDCProvider element with a persistent databaseStore
	 */
	public Element addOIDCProviderDatabaseStore(String specialSubjectType, String authenticatedGroupid, String filesetClassesDir, String filesetClassesIncludes, String filesetLibraryDir, String db2PropertiesCurrentSchema, String db2PropertiesdatabaseName, 
			String db2PropertiesPassword, String db2PropertiesPortNumber, String db2PropertiesServerName, String db2PropertiesUser);


	/**
	 * Adds the <keystore> element to server.xml 
	 * 
	 * @param location 
	 * @param password 
	 * 
	 * @return the keystore element
	 */
	public Element addKeystore(String location, String password);

	/**
	 * Adds the <keystore> element to server.xml 
	 * 
	 * @param id
	 * @param password
	 * @param type
	 * @param location
	 * @param filebased
	 * @param readOnly
	 * 
	 * @return the keystore element
	 */
	public Element addKeystore(String id, String password, String type, String location, String filebased, String readOnly);
	
	/**
	 * Adds the <ssl> element to server.xml 
	 * @param needTrustStore 
	 * 
	 * @return the ssl element
	 */
	public Element addSSL(Boolean needTrustStore);

	/**
	 * Add the <ssl> element to server.xml.
	 * 
	 * @param id
	 * @param keyStoreRef
	 * @param trustStoreRef
	 * @param aliasServer
	 * @return the ssl element
	 */
	public Element addSSL(String id, String keyStoreRef, String trustStoreRef, String aliasServer);
}
