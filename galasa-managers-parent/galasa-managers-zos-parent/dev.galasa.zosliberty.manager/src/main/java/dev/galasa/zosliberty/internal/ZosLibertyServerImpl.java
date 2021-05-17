/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty.internal;

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServer;
import dev.galasa.zosliberty.IZosLibertyServerXml;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyServerImpl implements IZosLibertyServer {

	private ZosLibertyImpl zosLiberty;
	private IZosFileHandler zosFileHandler;
	private IZosImage zosImage;
	private IZosUNIXFile wlpInstallDir;
	private IZosUNIXFile wlpUserDir;
	private String serverName = "defaultServer";
	private IZosLibertyServerXml serverXml;

	private static final String SLASH_SYBMOL = "/";
	
	public ZosLibertyServerImpl(ZosLibertyImpl zosLiberty, IZosImage zosImage, IZosUNIXFile wlpInstallDir, IZosUNIXFile wlpUserDir) throws ZosLibertyServerException {
		this.zosLiberty = zosLiberty;
		try {
			this.zosFileHandler = this.zosLiberty.getZosFileHandler();
		} catch (ZosLibertyManagerException e) {
			throw new ZosLibertyServerException("Unable to get zOS File Handler", e);
		}
		this.zosImage = zosImage;
		this.wlpInstallDir = wlpInstallDir;
		this.wlpUserDir = wlpUserDir;
		IZosUNIXFile serverXmlUnixFile;
		try {
			StringBuilder path = new StringBuilder();
			path.append(this.wlpUserDir.getUnixPath());
			path.append(SLASH_SYBMOL);
			path.append("servers");
			path.append(SLASH_SYBMOL);
			path.append(getServerName());
			path.append(SLASH_SYBMOL);
			path.append("server.xml");
			serverXmlUnixFile = this.zosFileHandler.newUNIXFile(path.toString(), zosImage);
		} catch (ZosUNIXFileException e) {
			throw new ZosLibertyServerException("Unable to create server.xml object", e);
		}
		this.serverXml = new ZosLibertyServerXmlImpl(this.zosFileHandler, this.zosImage, serverXmlUnixFile);
	}
	
	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	@Override
	public String getServerName() {
		return this.serverName;
	}

	@Override
	public void setWlpInstallDir(String wlpInstallDir) throws ZosLibertyServerException {
		try {
			this.wlpInstallDir = this.zosFileHandler.newUNIXFile(wlpInstallDir, this.zosImage);
		} catch (ZosUNIXFileException e) {
			throw new ZosLibertyServerException("Unable to set WLP_INSTALL_DIR", e);
		}
	}

	@Override
	public IZosUNIXFile getWlpInstallDir() {
		return this.wlpInstallDir;
	}

	@Override
	public void setWlpUserDir(String wlpUserDir) throws ZosLibertyServerException {
		try {
			this.wlpUserDir = this.zosFileHandler.newUNIXFile(wlpUserDir, this.zosImage);
		} catch (ZosUNIXFileException e) {
			throw new ZosLibertyServerException("Unable to set WLP_USER_DIR", e);
		}
	}

	@Override
	public IZosUNIXFile getWlpUserDir() {
		return this.wlpUserDir;
	}

	@Override
	public void setServerXml(IZosLibertyServerXml serverXml) throws ZosLibertyServerException {
		this.serverXml = serverXml;
	}

	@Override
	public IZosLibertyServerXml getServerXml() throws ZosLibertyServerException {
		return this.serverXml;
	}

	@Override
	public void loadServerXml() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getServerMessageLogs() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApplicationTag(String path, String id, String name, String type) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableDropins() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addBundleToDropins(String bundle, String targetFileName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeBundleFromDropins(String bundleName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDropinsDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanDropins() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLogsDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpServerConfigPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void build() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildWlpServerFromTemplate() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildWlpServerWithServerXML(String serverXMLArtifactPath, String serverName, Class<?> klass)
			throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildWlpServerBasic(String serverDescription, int httpPort, int httpsPort, String hostName)
			throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveServerXML() {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveInstalledAppsXML() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveJvmOptions() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public String checkpointLogs() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String checkpointLogs(String output) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getMsgLog(boolean binary) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getMsgLogSinceCheckpoint(boolean binary) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean waitForMessageInMsgLog(String message, String failMessage, long resourceTimeout,
			boolean useCheckpoint) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForMessageInMsgLog(String message) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForMessageInMsgLog(String message, long resourceTimeout) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean searchMessageLogForString(String stringValue, boolean binary) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean searchMessageLogForStringSinceCheckpoint(String stringValue, boolean binary)
			throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean searchMessageLogForText(String text, boolean binary) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean searchMessageLogForTextSinceCheckpoint(String text, boolean binary)
			throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForLibertyEnabled() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForLibertyDisabled() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OutputStream getOrderedMessageLogsContents(boolean useCheckpoint) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getOrderedMessageLogsContents() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getLatestMessageLogContents() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean searchLatestMessageLogForString(String stringValue) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean searchLatestMessageLogForText(String text) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForMessageInLatestMsgLog(String message, String failMessage, long resourceTimeout,
			boolean useCheckpoint) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addLocalBundleToDropins(String bundleLocation, String bundleFileName, Class<?> owningClass)
			throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getLibertyServerDump(String desiredDumpName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsFFDCs() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsFFDCs(boolean sinceCheckpoint) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void saveLibertyTraceFiles() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getWlpServerDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IZosUNIXFile getKeystoreFile() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerXmlDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpVersion() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document readXmlFromUSS(String xmlDirName, String xmlFileName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document readServerXmlFromUSS() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeXmlToUSS(Document xmlDoc, String xmlDirName, String xmlFileName) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeServerXmlToUSS() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeJvmOptionsUSS(String options) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

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
	public void clearWorkarea() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearLogs() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeServerFilesFromUSS() throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getWlpServerWorkareaDir() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpServerLogsDir() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getMessagesLog(boolean binary) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessagesLogString() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessagesLogName() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean searchMessagesLogForText(String text, boolean binary) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForMessagesLogText(String text) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForLibertyStart() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IWLPServerLogs getWlpServerLogs() throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String securityUtilityEncode(String password) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String securityUtilityEncode(String password, String javaHome) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String securityGenerateKeystore(String password) throws ZosLibertyServerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBundleToRepository(String dir, String includes) throws ZosLibertyServerException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String toString() {
		return "[zOS Liberty Server] " + this.serverName;
	}
}
