/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServerXml;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyServerXmlImpl implements IZosLibertyServerXml {
	
	private IZosUNIXFile serverXmlUnixfile;
	private Document serverXmlDocument;

	public ZosLibertyServerXmlImpl(IZosUNIXFile serverXmlFile) {
		this.serverXmlUnixfile = serverXmlFile;
		this.serverXmlUnixfile.setDataType(UNIXFileDataType.BINARY);
	}

	protected Document stringToDocument(String content) throws ZosLibertyServerException {
        try {
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder builder = factory.newDocumentBuilder();
	        return builder.parse(new InputSource(new StringReader(content)));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ZosLibertyServerException("Unable to convert server.xml java.lang.String to org.w3c.dom.Document");
		}
	}
	
	private String documentToString(Document document) throws ZosLibertyServerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
        	throw new ZosLibertyServerException("Unable to convert server.xml org.w3c.dom.Document to java.lang.String");
        }
    }

	@Override
	public void setServerXmlFromDocument(Document content) {
		this.serverXmlDocument = content;
	}

	@Override
	public void setServerXmlFromString(String content) throws ZosLibertyServerException {
		this.serverXmlDocument = stringToDocument(content);
	}

	@Override
	public void setServerXmlFromZosUNIXFile(IZosUNIXFile content) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		throw new ZosLibertyServerException("Method not implemented");
	}

	@Override
	public String getServerXmlAsString() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		throw new ZosLibertyServerException("Method not implemented");
	}

	@Override
	public Document getServerXmlAsDocument() {
		return this.serverXmlDocument;
	}

	@Override
	public IZosUNIXFile getServerXmlAsZosUNIXFile() throws ZosLibertyServerException {
		return this.serverXmlUnixfile;
	}

	@Override
	public void build() throws ZosLibertyServerException {
		try {
			if (!this.serverXmlUnixfile.exists()) {
				this.serverXmlUnixfile.setShouldCleanup(false);
				this.serverXmlUnixfile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
			}
			this.serverXmlUnixfile.setDataType(UNIXFileDataType.BINARY);
			this.serverXmlUnixfile.store(documentToString(this.serverXmlDocument));
		} catch (ZosUNIXFileException e) {
			throw new ZosLibertyServerException("Unable to build server.xml file on the zOS UNIX file system", e);
		}
	}
	
	@Override
	public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
		try {
			this.serverXmlUnixfile.saveToResultsArchive(rasPath);
		} catch (ZosUNIXFileException e) {
			throw new ZosLibertyServerException("Unable to store the content of the Liberty server.xml to the Results Archive Store", e);
		}
	}
	
	@Override
	public void addXmlElement(String elementName, String elementId, Map<String, String> elementAttributes) throws ZosLibertyServerException {
		Element element = this.serverXmlDocument.createElement(elementName);
		getServerParentNode().appendChild(element);
		element.setAttribute("id", elementId);
		if (elementAttributes != null) {
			for (Entry<String, String> entry : elementAttributes.entrySet()) {
				element.setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void removeXmlElement(String elementName) throws ZosLibertyServerException {
		NodeList elements = this.serverXmlDocument.getElementsByTagName(elementName);
		for (int i = 0; i < elements.getLength(); i++) {
			Node element = elements.item(i);
			element.getParentNode().removeChild(element);
		}		
	}

	@Override
	public void removeXmlElement(String elementName, String elementId) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		throw new ZosLibertyServerException("Method not implemented");		
	}

	private Node getServerParentNode() throws ZosLibertyServerException {
		if (this.serverXmlDocument == null) {
			throw new ZosLibertyServerException("server.xml content has not been set");
		}
		return this.serverXmlDocument.getElementsByTagName("server").item(0);
	}

	@Override
	public void loadFromFileSystem() throws ZosLibertyServerException {
		String serverXmlString;
		try {
			serverXmlString = this.serverXmlUnixfile.retrieve();
		} catch (ZosUNIXFileException e) {
			throw new ZosLibertyServerException("Unable to retrieve server.xml from zOS UNIX file system", e);
		}
		this.serverXmlDocument = stringToDocument(serverXmlString);
	}
	
	
	
	
	
	
	
	private Document parseServerXml(String serverXmlString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IServerXmlElement newServerXmlElement(String elementName, Map<String, String> elementAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDropinsEnabled(boolean enabled) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}
	
	
	
	
	
	

	@Override
	public void addApplicationTag(String path, String id, String name, String type) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDefaultHttpEndpoint(String host, String httpPort, String httpsPort) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCompoundXmlElement(String parent, String elementName, String elementId, Map<String, String> elementAttributes, IxmlElementList subElements) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCompoundXmlElementById(String parent, String parentId, String elementName, String elementId,
			Map<String, String> elementAttributes, IxmlElementList subElements) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDataSource(String id, String jndiName, Map<String, String> dataSourceAttributes,
			IxmlElementList dataSourceSubElements) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteDataSource(String id) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCICSDataSource(String id, String jndiName, Map<String, String> dataSourceAttributes,
			IxmlElementList dataSourceSubElements) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteCICSDataSource(String id) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLibrary(String libraryId, Map<String, String> libraryAttibutes, IxmlElementList librarySubElements)
			throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteLibrary(String id) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteXmlNode(Document xmlDoc, String xmlTag, String xmlItem, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteXmlNode(Document xmlDoc, String xmlTag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteXmlNodeById(Document xmlDoc, String xmlTag, String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addXmlNode(Document xmlDoc, String xmlTag, String parent, Map<String, String> nodeAttributes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addXmlNodeByItemNo(Document xmlDoc, String xmlTag, String parent, Map<String, String> nodeAttributes,
			int itemNo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addXmlNodeById(Document xmlDoc, String xmlTag, String parent, String parentId,
			Map<String, String> nodeAttributes) {
		// TODO Auto-generated method stub

	}

	@Override
	public String printXmlDoc(Document xmlDoc) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String printServerXmlDoc() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String printServerXmlFromUss() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deployAppToDropins(String appFileName, Class<?> artifactClass) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deployAppToDropins(InputStream appInputStream, String appFileName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deployAppToApps(String appFileName, Class<?> artifactClass) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deployAppToApps(InputStream appInputStream, String appFileName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addApplicationServerXML(String id, String name, String location, String type)
			throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public Node getXmlNode(Document xmlDoc, String xmlTag, String nodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHttpEndpointHost(Document serverXML) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHttpEndpointHttpPort(Document serverXML) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHttpEndpointHttpsPort(Document serverXML) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addFeatureServerXML(String... features) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean removeFeatureServerXML(String featureName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> getEnabledFeatures(Document serverXML) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFeatureEnabled(String featureName, Document serverXML) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IxmlElementList createIxmlElementList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBundleToRepository(String dir, String includes) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeElements(String... elements) {
		// TODO Auto-generated method stub

	}

	@Override
	public Element addBasicRegistry(String userid, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addBasicRegistry(String userid, String password, String group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSafRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSafCredentials(String profilePrefix, String unauthenticatedUser,
			boolean mapDistributedIdentities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSafCredentials(String profilePrefix, boolean mapDistributedIdentities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSafCredentials(String profilePrefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSafRoleMapper(String applid, String rolePattern, boolean toUpperCase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSafAuthorization() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addOIDCProviderLocalStore(URL rpOrigin, String specialSubjectType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addOIDCProviderLocalStore(URL rpOrigin, String issuerIdentifier, String specialSubjectType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addOIDCClient(URL opOrigin, boolean mapIdentityToRegistryUser) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addOIDCClient(URL opOrigin, boolean mapIdentityToRegistryUser, String issuerIdentifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addApplication(String type, String appsDir, String app) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addApplicationBndWithUser(Element application, String protectingRole, String user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addApplicationBndWithGroup(Element application, String protectingRole, String group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addApplicationBndAllAuthenticated(Element application, String protectingRole,
			String specialSubjectType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disableAuthCache() {
		// TODO Auto-generated method stub

	}

	@Override
	public Element addConfig(String onError, String monitorInterval, String updateTrigger) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addLdapRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addTraceSpecification(String traceString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addDb2Type4DataSource(String filesetClassesDir, String filesetClassesIncludes,
			String filesetLibraryDir, String db2PropertiesCurrentSchema, String db2PropertiesdatabaseName,
			String db2PropertiesPassword, String db2PropertiesPortNumber, String db2PropertiesServerName,
			String db2PropertiesUser) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addOIDCProviderDatabaseStore(String specialSubjectType, String authenticatedGroupid,
			String filesetClassesDir, String filesetClassesIncludes, String filesetLibraryDir,
			String db2PropertiesCurrentSchema, String db2PropertiesdatabaseName, String db2PropertiesPassword,
			String db2PropertiesPortNumber, String db2PropertiesServerName, String db2PropertiesUser) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addKeystore(String location, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addKeystore(String id, String password, String type, String location, String filebased,
			String readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSSL(Boolean needTrustStore) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element addSSL(String id, String keyStoreRef, String trustStoreRef, String aliasServer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return "[zOS Liberty server.xml] " + (this.serverXmlUnixfile != null? this.serverXmlUnixfile.getUnixPath(): "");
	}

}
