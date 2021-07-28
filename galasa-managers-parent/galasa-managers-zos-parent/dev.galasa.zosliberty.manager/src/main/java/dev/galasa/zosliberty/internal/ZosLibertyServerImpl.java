/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServer;
import dev.galasa.zosliberty.IZosLibertyServerLogs;
import dev.galasa.zosliberty.IZosLibertyServerXml;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.ZosLibertyServerException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

public class ZosLibertyServerImpl implements IZosLibertyServer {

	private static final Log logger = LogFactory.getLog(ZosLibertyServerImpl.class);
	
    private ZosLibertyImpl zosLiberty;
	private ZosLibertyManagerImpl zosLibertyManager;
    private IZosFileHandler zosFileHandler;
	private IZosBatch zosBatch;
    private IZosConsole zosConsole;
    private IZosUNIXCommand zosUNIXCommand;
	private IArtifactManager artifactManager;
    private IZosImage zosImage;
	private IZosUNIXFile wlpInstallDir;
    private IZosUNIXFile wlpUserDir;
    private IZosUNIXFile wlpOutputDir;
	private IZosUNIXFile dropinsDir;
    private IZosUNIXFile javaHome;
	private IZosUNIXFile logsDir;
    private String serverName = "defaultServer";
    private IZosLibertyServerXml serverXml;
    private IZosLibertyServerLogs libertyServerLogs;
	private IZosBatchJob zosLibertySeverJob;
	private IZosUNIXFile tmpWorkDirDir;
	private IZosUNIXFile sharedAppDir;
    
	private static final String SLASH_SYBMOL = "/";
	private static final String SEMI_COLON_SYMBOL = ";";
    
    public ZosLibertyServerImpl(ZosLibertyImpl zosLiberty, IZosImage zosImage, String wlpInstallDir, String wlpUserDir, String wlpOutputDir) throws ZosLibertyServerException {
        this.zosLiberty = zosLiberty;
        try {
			this.zosLibertyManager = this.zosLiberty.getZosLibertyManager();
		} catch (ZosLibertyManagerException e) {
			throw new ZosLibertyServerException("Unable to get zOS Liberty Manager", e);
		}
        try {
            this.zosFileHandler = this.zosLibertyManager.getZosFileHandler();
        } catch (ZosLibertyManagerException e) {
            throw new ZosLibertyServerException("Unable to get zOS File Handler", e);
        }
        this.zosUNIXCommand = this.zosLibertyManager.getZosUNIXCommand(zosImage);
        this.zosBatch = this.zosLibertyManager.getZosBatch(zosImage);
        try {
            this.zosConsole = this.zosLibertyManager.getZosConsole(zosImage);
        } catch (ZosLibertyManagerException e) {
            throw new ZosLibertyServerException("Unable to get zOS Console", e);
        }
        this.artifactManager = this.zosLibertyManager.getArtifactManager();
        this.zosImage = zosImage;
        if (wlpInstallDir == null) {
            getWlpInstallDir();
        } else {
            setWlpInstallDir(wlpInstallDir);
        }
        if (wlpUserDir == null) {
            getWlpUserDir();
        } else {
            setWlpUserDir(wlpUserDir);
        }
        if (wlpOutputDir == null) {
            getWlpOutputDir();
        } else {
            setWlpOutputDir(wlpOutputDir);
        }
        this.serverXml = new ZosLibertyServerXmlImpl(getServerXmlUnixFile());
    }
    
    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void setWlpInstallDir(String wlpInstallDir) throws ZosLibertyServerException {
        try {
            this.wlpInstallDir = this.zosFileHandler.newUNIXFile(toDirectory(wlpInstallDir), zosImage);
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create WLP install directory zOS UNIX file object", e);
        }
    }

    @Override
    public void setWlpUserDir(String wlpUserDir) throws ZosLibertyServerException {
        try {
            this.wlpUserDir = this.zosFileHandler.newUNIXFile(toDirectory(wlpUserDir), getZosImage());
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create WLP user directory zOS UNIX file object", e);
        }
    }

    @Override
    public void setWlpOutputDir(String wlpOutputDir) throws ZosLibertyServerException {
        try {
            this.wlpOutputDir = this.zosFileHandler.newUNIXFile(toDirectory(wlpOutputDir), getZosImage());
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create WLP output directory zOS UNIX file object", e);
        }
    }

    @Override
    public void setJavaHome(String javaHome) throws ZosLibertyServerException {
        try {
            this.javaHome = this.zosFileHandler.newUNIXFile(toDirectory(javaHome), getZosImage());
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to create Java Home zOS UNIX file object", e);
        }
    }

    @Override
    public void build() throws ZosLibertyServerException {
        if (getServerXml() != null) {
            getServerXml().build();
        }
    }

    @Override
    public String getVersion() throws ZosLibertyServerException {
        try {
            UnixCommandResponse commandResponse = issueLibertyCommand("productInfo version", false);
            int rc = commandResponse.getRc();
            if (rc != 0) {
            	throw new ZosLibertyServerException("Invalid return code from the productInfo script: RC=" + rc);
            }
            Pattern pattern = Pattern.compile(".*Product version:\\s*([^\\n\\r]*)", Pattern.MULTILINE);
            String response = commandResponse.getResponse();
            Matcher matcher = pattern.matcher(response);
            if (matcher.find() && matcher.groupCount() > 0) {
            	return matcher.group(1);
            } else {
                throw new ZosLibertyServerException("Unable to get Liberty version from the productInfo script:\n" + response);
            }
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get Liberty version", e);
        }
    }

	@Override
    public String getServerName() {
        if (this.serverName == null) {
            this.serverName = "defaultServer";
        }
        return this.serverName;
    }

    @Override
    public IZosUNIXFile getWlpInstallDir() throws ZosLibertyServerException {
        if (this.wlpInstallDir == null) {
            try {
            	if (getZosImage().getLibertyInstallDir() == null) {
            		throw new ZosLibertyServerException("The Liberty install directory not been set and a value has not been supplied in the CPS for zOS image " + getZosImage().getImageID());
            	}
                setWlpInstallDir(getZosImage().getLibertyInstallDir());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get Liberty install directory", e);
            }
        }
        return this.wlpInstallDir;
    }

    @Override
    public IZosUNIXFile getWlpUserDir() throws ZosLibertyServerException {
        if (this.wlpUserDir == null) {
            try {
            	IZosUNIXFile runTemporaryUNIXPath = this.zosFileHandler.newUNIXFile(getZosImage().getRunTemporaryUNIXPath(), getZosImage());
            	if (!runTemporaryUNIXPath.exists()) {
            		runTemporaryUNIXPath.create(PosixFilePermissions.fromString("rwxrwxrwx"));
            		runTemporaryUNIXPath.setShouldCleanup(true);
            	}
                StringBuilder path = new StringBuilder();
                path.append(runTemporaryUNIXPath);
                path.append(SLASH_SYBMOL);
                path.append("wlp");
                path.append(SLASH_SYBMOL);
                path.append("usr");
                setWlpUserDir(path.toString());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get Liberty user directory", e);
            }
        }
        return this.wlpUserDir;
    }

    @Override
    public IZosUNIXFile getWlpOutputDir() throws ZosLibertyServerException {
        if (this.wlpOutputDir == null) {
            StringBuilder path = new StringBuilder();
            path.append(getWlpUserDir());
            path.append(SLASH_SYBMOL);
            path.append("servers");
            path.append(SLASH_SYBMOL);
            path.append(getServerName());
            path.append(SLASH_SYBMOL);
            setWlpOutputDir(path.toString());
        }
        return this.wlpOutputDir;
    }
    
    @Override
    public IZosUNIXFile getJavaHome() throws ZosLibertyServerException {
        if (this.javaHome == null) {
            try {
            	if (getZosImage().getJavaHome() == null) {
            		throw new ZosLibertyServerException("The Java Home directory not been set and a value has not been supplied in the CPS for zOS image " + getZosImage().getImageID());
            	}
                setJavaHome(getZosImage().getJavaHome());
            } catch (ZosManagerException e) {
                throw new ZosLibertyServerException("Unable to get Java Home directory", e);
            }
        }
        return this.javaHome;
    }

    @Override
	public IZosImage getZosImage() {
		return this.zosImage;
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
    public IZosLibertyServerXml loadServerXmlFromFileSystem() throws ZosLibertyServerException {
        getServerXml().loadFromFileSystem();
        return getServerXml();
    }

    @Override
    public IZosUNIXFile getLogsDirectory() throws ZosLibertyServerException {
        if (this.logsDir == null) {
            try {
                this.logsDir = this.zosFileHandler.newUNIXFile(getWlpOutputDir().getUnixPath() + SLASH_SYBMOL + "logs/", getZosImage());
            } catch (ZosUNIXFileException e) {
                throw new ZosLibertyServerException("Unable to get logs directory", e);
            }
    
        }
        return this.logsDir;
    }

    @Override
    public IZosLibertyServerLogs getLogs() throws ZosLibertyServerException {
        if (this.libertyServerLogs == null) {
            try {
                this.libertyServerLogs = new ZosLibertyServerLogsImpl(getLogsDirectory());
            } catch (ZosLibertyServerException e) {
                throw new ZosLibertyServerException("Unable to get server logs", e);
            }
        }
        return this.libertyServerLogs;
    }

    @Override
    public String getMessageLog() throws ZosLibertyServerException {
        try {
            return new String(getLogs().getMessagesLog().retrieveAsBinary(), StandardCharsets.UTF_8);
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to get messages.log", e);
        }
    }

	@Override
	public int create() throws ZosLibertyServerException {
        try {
            return issueLibertyCommand("server create " + getServerName(), false).getRc();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get create Liberty server", e);
        }
	}

	@Override
	public int start() throws ZosLibertyServerException {
        try {
            return issueLibertyCommand("server start " + getServerName(), false).getRc();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to start Liberty server", e);
        }
	}

	@Override
	public boolean run() throws ZosLibertyServerException {
    	try {
			this.zosLibertySeverJob = this.zosBatch.submitJob(buildServerJcl(), null);
			if (waitForStart() != 0) {
				if (this.zosLibertySeverJob.getStatus() != JobStatus.ACTIVE) {
					throw new ZosLibertyServerException("Liberty server batch job ended " + this.zosLibertySeverJob.getRetcode());
				}
			}
			return true;
		} catch (ZosBatchException | ZosLibertyServerException e) {
			throw new ZosLibertyServerException("Unable to start Liberty server", e);
		}
	}

	@Override
	public int waitForStart() throws ZosLibertyServerException {
		return waitForStart(20000);
	}

	@Override
	public int waitForStart(int millisecondTimeout) throws ZosLibertyServerException {
		logger.trace("Waiting " + millisecondTimeout + "ms for Liberty server " +  getServerName() + " to start");
	    int timeout = millisecondTimeout/1000;
	    for (int i = 0; i < timeout; i++) {
	    	if (this.zosLibertySeverJob == null || this.zosLibertySeverJob.getStatus() == JobStatus.ACTIVE) {
	    		if (status() == 0) {
	    			return 0;
	    		}
            } else {
            	JobStatus jobStatus = this.zosLibertySeverJob.getStatus();
            	if (jobStatus == JobStatus.OUTPUT ||
            		jobStatus == JobStatus.NOTFOUND ||
            		jobStatus == JobStatus.UNKNOWN) {
            		return status();
            	}
            }
        }
		return status();
	}

	@Override
	public int waitForStop() throws ZosLibertyServerException {
		return waitForStop(10000);
	}

	@Override
	public int waitForStop(int millisecondTimeout) throws ZosLibertyServerException {
		logger.trace("Waiting " + millisecondTimeout + "ms for Liberty server " +  getServerName() + " to stop");
	    int timeout = millisecondTimeout/1000;
	    for (int i = 0; i < timeout; i++) {
	    	if (status() == 1) {
	    		return 1;
            }
        }
		return status();
	}

	@Override
	public int stop() throws ZosLibertyServerException {
		try {
    		if (this.zosLibertySeverJob != null) {
    			stopJob();
    			return 0;
    		} else {
				return issueLibertyCommand("server stop " + getServerName(), false).getRc();
    		}
		} catch (ZosLibertyServerException e) {
			throw new ZosLibertyServerException("Problem stopping Liberty server", e);
		}
	}

	@Override
	public int status() throws ZosLibertyServerException {
        try {
            return issueLibertyCommand("server status " + getServerName(), false).getRc();
        } catch (ZosLibertyServerException e) {
            throw new ZosLibertyServerException("Unable to get Liberty server status", e);
        }
	}
	
	@Override
	public void delete() throws ZosLibertyServerException {
		try {
			if (getWlpOutputDir().exists()) {
				getWlpOutputDir().directoryDeleteNonEmpty();
			}
		} catch (ZosUNIXFileException | ZosLibertyServerException e) {
			logger.info("Problem deleting Liberty output directory", e);
		}
		try {
			if (getWlpUserDir().exists()) {
				getWlpUserDir().directoryDeleteNonEmpty();
			}
		} catch (ZosUNIXFileException | ZosLibertyServerException e) {
			logger.info("Problem deleting Liberty user directory", e);
		}
	}
	
	@Override
	public void setDefaultHttpEndpoint(String host, int httpPort, int httpsPort) throws ZosLibertyServerException {
		loadServerXmlFromFileSystem().removeElementsById("httpEndpoint", "defaultHttpEndpoint");
		Map<String, String> attributes = new HashMap<>();
		attributes.put("id", "defaultHttpEndpoint");
		if (host != null) {
			attributes.put("host", host);
		}
		if (httpPort > 0) {
			attributes.put("httpPort", String.valueOf(httpPort));
		}
		if (httpsPort > 0) {
			attributes.put("httpsPort", String.valueOf(httpsPort));
		}
		getServerXml().addElement("httpEndpoint", attributes);
		getServerXml().build();
	}
	
	@Override
	public void deployApplication(String path, Class<?> clazz, String location, ApplicationType type, String name, String contextRoot) throws ZosLibertyServerException {
		try {
			if (location == null) {
				location = "${shared.app.dir}";
			}
			String parsedLocation = resolveLocation(location);
			copyApplicationToZosUnix(path, clazz, parsedLocation);
			Map<String, String> attributes = new HashMap<>();
			attributes.put("id", name);
			attributes.put("name", name);
			attributes.put("type", type.toString());
			attributes.put("location", location);
			if (contextRoot != null) {
				attributes.put("context-root", contextRoot);			
			}
			getServerXml().addElement("application", attributes);
			getServerXml().build();
		} catch (ZosLibertyServerException e) {
			throw new ZosLibertyServerException("Problem deploying application", e);
		}
	}
	
	private String resolveLocation(String location) throws ZosLibertyServerException {
		String parsedLocation = location;
		try {
			parsedLocation = parsedLocation.replace("${shared.app.dir}", getSharedAppDir().getUnixPath());
		} catch (ZosLibertyServerException e) {
			throw new ZosLibertyServerException("Problem parsing location String '" + location + "'", e);
		}
		return parsedLocation;
	}

	@Override
	public void deployApplicationToDropins(String path, Class<?> clazz) throws ZosLibertyServerException {
		try {
			copyApplicationToZosUnix(path, clazz, getDropinsDir().getUnixPath());
		} catch (ZosLibertyServerException e) {
			throw new ZosLibertyServerException("Problem deploying application to dropins", e);
		}
	}
	
	public void copyApplicationToZosUnix(String path, Class<?> clazz, String location) throws ZosLibertyServerException {
		IBundleResources resources = this.artifactManager.getBundleResources(clazz);
        InputStream application;
		try {
			application = resources.retrieveFile(path);
		} catch (TestBundleResourceException e) {
			throw new ZosLibertyServerException("Problem getting resource '" + path + "' from test bundle", e);
		}
		try {
			String fileName = new File(path).getName();
			IZosUNIXFile applicationFile = this.zosFileHandler.newUNIXFile(getTempWorkDir().getUnixPath() + fileName, getZosImage());
			applicationFile.setDataType(UNIXFileDataType.BINARY);
			if (!applicationFile.exists()) {
				applicationFile.create();
			}
			applicationFile.storeBinary(IOUtils.toByteArray(application));
			IZosUNIXFile locationFile = this.zosFileHandler.newUNIXFile(location + SLASH_SYBMOL, getZosImage());
			if (!locationFile.exists()) {
				locationFile.create();
			}
			String command = "cp " + getTempWorkDir().getUnixPath() + fileName + " " + location;
			UnixCommandResponse response = issueUnixCommand(command);
			if (response.getRc() != 0) {
				throw new ZosLibertyServerException("Problem copying application file to '" + location + "'");
			}
		} catch (ZosUNIXFileException | IOException e) {
			throw new ZosLibertyServerException("Problem deploying application to '" + location + "'", e);
		}
	}
	
	@Override
	public IZosUNIXFile getDropinsDir() throws ZosLibertyServerException {
		if (this.dropinsDir == null) {
	        try {
	        	this.dropinsDir = this.zosFileHandler.newUNIXFile(getWlpOutputDir().getUnixPath() + SLASH_SYBMOL + "dropins/", getZosImage());
	        } catch (ZosUNIXFileException e) {
	            throw new ZosLibertyServerException("Unable to get dropins directory", e);
	        }
		}
		return this.dropinsDir;
	}
	
    private void DELETEME() {
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
    public void cleanDropins() throws ZosLibertyServerException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getWlpServerConfigPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void buildWlpServerFromTemplate() throws ZosLibertyServerException {
        // TODO Auto-generated method stub

    }

    @Override
    public void buildWlpServerWithServerXML(String serverXMLArtifactPath, String serverName, Class<?> klass) throws ZosLibertyServerException {
        // TODO Auto-generated method stub

    }

    @Override
    public void buildWlpServerBasic(String serverDescription, int httpPort, int httpsPort, String hostName)    throws ZosLibertyServerException {
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
    public boolean waitForMessageInMsgLog(String message, String failMessage, long resourceTimeout,    boolean useCheckpoint) throws ZosLibertyServerException {
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
    public boolean searchMessageLogForTextSinceCheckpoint(String text, boolean binary) throws ZosLibertyServerException {
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
    public boolean waitForMessageInLatestMsgLog(String message, String failMessage, long resourceTimeout, boolean useCheckpoint) throws ZosLibertyServerException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addLocalBundleToDropins(String bundleLocation, String bundleFileName, Class<?> owningClass) throws ZosLibertyServerException {
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
    public void addApplicationServerXML(String id, String name, String location, String type) throws ZosLibertyServerException {
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

    
    private void DELETEMEAGAIN() {
    }
    
    @Override
    public String toString() {
        return "[zOS Liberty Server] " + this.serverName;
    }

    @Override
    public void saveToResultsArchive() throws ZosLibertyServerException {
        saveToResultsArchive(getDefaultRasPath());
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
        if (this.zosLibertySeverJob != null) {
        	rasPath = archiveJob(rasPath);
        }
        getLogs().saveToResultsArchive(rasPath);
        try {
			if (getServerXml().getAsZosUNIXFile().exists()) {
				getServerXml().saveToResultsArchive(rasPath);
			}
		} catch (ZosUNIXFileException | ZosLibertyServerException e) {
			throw new ZosLibertyServerException("Unable to save server.xml zOS UNIX file", e);
		}
    }

	private void stopJob() throws ZosLibertyServerException {
		try {
			this.zosConsole.issueCommand("RO " + this.zosImage.getImageID() + ",STOP " + this.zosLibertySeverJob.getJobname().getName());
			waitForStop();
		} catch (ZosConsoleException | ZosLibertyServerException e) {
			logger.error("Problem stopping Liberty server job", e);
		}
    	try {
    		saveToResultsArchive(getDefaultRasPath());
        	this.zosLibertySeverJob.purge();
        	this.zosLibertySeverJob = null;
		} catch (ZosBatchException e) {
			logger.error("Problem during Liberty server job clean up", e);
		}
	}

    private String archiveJob(String rasPath) throws ZosLibertyServerException {
    	try {
    		rasPath = rasPath + SLASH_SYBMOL +
    				  "serverBatchJobs" + SLASH_SYBMOL + 
    				  this.zosLibertySeverJob.getJobname().getName() + "_" + 
    				  this.zosLibertySeverJob.getJobId() + "_" + 
    				  this.zosLibertySeverJob.getRetcode().replace(" ", "-").replace("????", "UNKNOWN");
    		this.zosLibertySeverJob.saveOutputToResultsArchive(rasPath);
    		return rasPath;
		} catch (ZosBatchException e) {
			throw new ZosLibertyServerException("Problem during archive of Liberty server batch job", e);
		}
	}

	private IZosUNIXFile getServerXmlUnixFile() throws ZosLibertyServerException {
	    IZosUNIXFile serverXmlUnixFile;
	    try {
	        StringBuilder path = new StringBuilder();
	        path.append(getWlpUserDir().getUnixPath());
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
	    return serverXmlUnixFile;
	}

	private UnixCommandResponse issueLibertyCommand(String command, boolean outputDir) throws ZosLibertyServerException {
		try {
			command = serverEnv(outputDir) + getWlpInstallDir().getUnixPath() + "bin/" + command;
			return issueUnixCommand(command);
		} catch (ZosLibertyServerException e) {
			throw new ZosLibertyServerException("Problem issuing Liberty command", e);
		}
	}

	private UnixCommandResponse issueUnixCommand(String command) throws ZosLibertyServerException {
		try {
			command = command + ";echo RC=$?";
			return new UnixCommandResponse(this.zosUNIXCommand.issueCommand(command));
		} catch (ZosUNIXCommandException e) {
			throw new ZosLibertyServerException("Problem issuing zOS UNIX command", e);
		}
	}

	private String serverEnv(boolean outputDir) throws ZosLibertyServerException {
		StringBuilder envs = new StringBuilder();
		envs.append("export JAVA_HOME=");
		envs.append(getJavaHome());
		envs.append(SEMI_COLON_SYMBOL);
		envs.append("export WLP_USER_DIR=");
		envs.append(getWlpUserDir());
		envs.append(SEMI_COLON_SYMBOL);
		if (outputDir) {
			envs.append("export WLP_OUTPUT_DIR=");
			envs.append(getWlpOutputDir());
			envs.append(SEMI_COLON_SYMBOL);
		}
		return envs.toString();
	}

	private String buildServerJcl() throws ZosLibertyServerException {
		return "// SET NAME='" + getServerName() + "'\n"
			 + "// SET INSDIR='" + getWlpInstallDir() + "'\n"
			 + "// SET USRDIR='" + getWlpUserDir() + "'\n"
			 + "//SERVER  EXEC PGM=BPXBATSL,REGION=0M,TIME=NOLIMIT,\n"
			 + "//  PARM='PGM &INSDIR./lib/native/zos/s390x/bbgzsrv &NAME'\n"
			 + "//WLPUDIR  DD PATH='&USRDIR.'\n"
			 + "//STDENV   DD *\n"
			 + "JAVA_HOME=" + getJavaHome() + "\n"
			 + "//STDOUT   DD SYSOUT=*\n"
			 + "//STDERR   DD SYSOUT=*\n";
	}

	private String toDirectory(String dir) {
        return dir.endsWith(SLASH_SYBMOL)? dir: dir + SLASH_SYBMOL;
    }

    private String getDefaultRasPath() {
    	return this.zosLibertyManager.getCurrentTestMethodArchiveFolder().toString();
    }
	
	private IZosUNIXFile getTempWorkDir() throws ZosLibertyServerException {
		if (this.tmpWorkDirDir == null) {
	        try {
	        	this.tmpWorkDirDir = this.zosFileHandler.newUNIXFile(getZosImage().getRunTemporaryUNIXPath() + "tmp/", getZosImage());
	        } catch (ZosManagerException e) {
	            throw new ZosLibertyServerException("Unable to get temporary work directory", e);
	        }
		}
		return this.tmpWorkDirDir;
	}

	private IZosUNIXFile getSharedAppDir() throws ZosLibertyServerException {
		if (this.sharedAppDir == null) {
	        try {
	        	this.sharedAppDir = this.zosFileHandler.newUNIXFile(getWlpUserDir().getUnixPath() + "shared/apps/", getZosImage());
	        } catch (ZosManagerException e) {
	            throw new ZosLibertyServerException("Unable to get shared app directory", e);
	        }
		}
		return this.sharedAppDir;
	}
	
	class UnixCommandResponse {
		private String response;
		private int rc;

		UnixCommandResponse(String commandResponse) throws ZosLibertyServerException {
			int lastPos = commandResponse.lastIndexOf("RC=");
			if (lastPos >= 0) {
				this.response = commandResponse.substring(0, lastPos);
				try {
					this.rc = Integer.parseInt(commandResponse.substring(lastPos+3).trim());
				} catch (NumberFormatException e) {
					throw new ZosLibertyServerException("Problem getting return code from zOS UNIX command (" + commandResponse.substring(lastPos).trim() + ")", e);
				}
			} else {
				throw new ZosLibertyServerException("Problem getting return code from zOS UNIX command");
			}
		}
		
		int getRc() {
			return this.rc;
		}
		
		String getResponse() {
			return this.response;
		}
	}
}
