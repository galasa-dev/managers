/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.resource.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.CicsResourceStatus;
import dev.galasa.cicsts.cicsresource.IJvmprofile;
import dev.galasa.cicsts.cicsresource.IJvmserver;
import dev.galasa.cicsts.cicsresource.IJvmserverLog;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.IZosLibertyServer;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class JvmserverImpl implements IJvmserver {
	
	private static final Log logger = LogFactory.getLog(JvmserverImpl.class);

	private CicsResourceManagerImpl cicsResourceManager;
	private IZosFileHandler zosFileHandler;
	private IZosLiberty zosLiberty;

	private ICicsRegion cicsRegion;
	private ICicsTerminal cicsTerminal;
	private IZosBatchJob cicsRegionJob;
	private String cicsApplid;
	private IZosImage cicsZosImage;
	private ICredentials cicsZosImageDefaultCredentials;
	private String cicsZosImageDefaultCredentialsUserid;
	private String cicsConfigroot;
	private String cicsJvmprofileDir;
	private String cicsUsshome;
	private String cicsRegionHomeDirectory;
	private String cicsRegionJobname;
	private String cicsRegionUserid;
	
	private String resourceDefinitionName;
	private String resourceDefinitionGroup;
	private String resourceDefinitionDescription;
	private CicsResourceStatus resourceDefinitionStatus = CicsResourceStatus.ENABLED;
	private String resourceDefinitionJvmprofile;
	private JvmserverType jvmserverType = JvmserverType.UNKNOWN;
	private HashMap<String, String> resourceDefinitionAttribute = new HashMap<>();
	private String resourceDefinitionLerunopts;
	private int resourceDefinitionThreadlimit = 15;
	
	private IJvmprofile jvmprofile;

	private String defaultWorkingDirectoryValue;
	private String defaultJavaHomeValue;
	private String defaultWlpInstallDirValue;
	private String defaultWlpUserDirValue;

	private IZosUNIXFile workingDirectory;
	private IZosUNIXFile diagnosticsDirectory;
	private IZosUNIXFile javaHome;

	private IZosUNIXFile logsDirectory;	
	private IJvmserverLog jvmLogLog;	
	private IJvmserverLog stdOutLog;	
	private IJvmserverLog stdErrLog;	
	private IJvmserverLog jvmTraceLog;
	
	private IZosLibertyServer zosLibertyServer;
	private IZosUNIXFile wlpInstallDir;
	private IZosUNIXFile wlpUserDir;

	private int defaultTimout = -1;

	private static final String SLASH_SYBMOL = "/";	
	private static final String SYMBOL_APPLID = "&APPLID;";
	private static final String SYMBOL_CONFIGROOT = "&CONFIGROOT;";
	private static final String SYMBOL_JVMSERVER = "&JVMSERVER;";
	private static final String SYMBOL_USSHOME = "&USSHOME;";
	private static final String SYMBOL_DATE = "&DATE;";
	private static final String SYMBOL_TIME = "&TIME;";

	private static final String OPTION_JAVA_HOME = "JAVA_HOME";
	private static final String OPTION_WORK_DIR = "WORK_DIR";
	
	private static final String RESOURCE_TYPE_JVMSERVER = "JVMSERVER";
	
	private static final String LOG_CEMT_PARAMETER = "Log";
	private static final String LOG_PROFILE_OPTION = "JVMLOG";
	private static final String LOG_FILE_SUFFIX = "dfhjvmlog";
	private static final String STDOUT_CEMT_PARAMETER = "Stdout";
	private static final String STDOUT_PROFILE_OPTION = "STDOUT";
	private static final String STDOUT_FILE_SUFFIX = "dfhjvmout";
	private static final String STDERR_CEMT_PARAMETER = "Stderr";
	private static final String STDERR_PROFILE_OPTION = "STDERR";
	private static final String STDERR_FILE_SUFFIX = "dfhjvmerr";
	private static final String TRACE_CEMT_PARAMETER = "Trace";
	private static final String TRACE_PROFILE_OPTION = "JVMTRACE";
	private static final String TRACE_FILE_SUFFIX = "dfhjvmtrc";
	private static final String SLASH = "/";

	public JvmserverImpl(CicsResourceManagerImpl cicsResourceManager, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, String name, String group, String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmserverResourceException {
		this.cicsResourceManager = cicsResourceManager;
		try {
			this.zosFileHandler = this.cicsResourceManager.getZosFileHandler();
		} catch (CicsResourceManagerException e) {
			throw new CicsJvmserverResourceException("Unable to get zOS File Handler", e);
		}
		this.cicsRegion = cicsRegion;
		this.cicsZosImage = cicsRegion.getZosImage();
		this.cicsTerminal = cicsTerminal;
		this.resourceDefinitionName = name;
		this.resourceDefinitionGroup = group;
		this.resourceDefinitionJvmprofile = jvmprofileName;
		this.jvmserverType = jvmserverType;
		this.jvmprofile = newJvmprofileFromCicsSuppliedProfile(jvmprofileName);
		try {
			this.javaHome = this.zosFileHandler.newUNIXFile(getDefaultJavaHomeValue(), cicsZosImage);
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Unable to set JAVA_HOME", e);
		}
		this.jvmprofile.setProfileValue(OPTION_JAVA_HOME, this.javaHome.getUnixPath());
		try {
			this.workingDirectory = this.zosFileHandler.newUNIXFile(getDefaultWorkingDirectoryValue(), cicsZosImage);
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Unable to set WORK_DIR", e);
		}
		this.jvmprofile.setProfileValue(OPTION_WORK_DIR, this.workingDirectory.getUnixPath());
		this.jvmprofile.setJvmProfileDir(getJvmProfileDir());
		this.jvmprofile.printProfile();
		if (isLiberty()) {
			try {
				this.wlpInstallDir = this.zosFileHandler.newUNIXFile(absolutePath(parseJvmprofileSymbols(this.jvmprofile.getWlpInstallDir())), this.cicsZosImage);
			} catch (ZosUNIXFileException e) {
				throw new CicsJvmserverResourceException("Unable to set Liberty install directory ($WLP_INSTALL_DIR)", e);
			}
			this.jvmprofile.setProfileValue("WLP_USER_DIR", getDefaultWlpUserDirValue());
			try {
				this.wlpUserDir = this.zosFileHandler.newUNIXFile(absolutePath(parseJvmprofileSymbols(this.jvmprofile.getWlpUserDir())), this.cicsZosImage);
			} catch (ZosUNIXFileException e) {
				throw new CicsJvmserverResourceException("Unable to set Liberty user directory ($WLP_USER_DIR)", e);
			}
			this.zosLibertyServer = newZosLibertyServerFromCicsSuppliedServerXml();
		}
	}

	public JvmserverImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile) throws CicsJvmserverResourceException {
		this.cicsResourceManager = cicsResourceManagerImpl;
		this.cicsRegion = cicsRegion;
		this.cicsTerminal = cicsTerminal;
		this.resourceDefinitionName = name;
		this.resourceDefinitionGroup = group;
		this.resourceDefinitionJvmprofile = jvmprofile.getProfileName();
		this.jvmserverType = determineJvmserverType();
	}

	public JvmserverImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile, IZosLibertyServer libertyServer) throws CicsJvmserverResourceException {
		this.cicsResourceManager = cicsResourceManagerImpl;
		this.cicsRegion = cicsRegion;
		this.cicsTerminal = cicsTerminal;
		this.resourceDefinitionName = name;
		this.resourceDefinitionGroup = group;
		this.resourceDefinitionJvmprofile = jvmprofile.getProfileName();
		this.jvmserverType = JvmserverType.LIBERTY;
		this.zosLibertyServer = libertyServer;
	}

	protected ILogScanner newLogScanner() throws CicsResourceManagerException {
		return this.cicsResourceManager.getLogScanner();
	}

	protected boolean isLiberty() {
		return this.jvmserverType == JvmserverType.LIBERTY;
	}

	protected IZosLibertyServer newZosLibertyServerFromCicsSuppliedServerXml() throws CicsJvmserverResourceException {
		IZosLibertyServer libertyServer;
		try {
			libertyServer = getLiberty().newZosLibertyServer(this.cicsZosImage, this.wlpInstallDir, this.wlpUserDir);
			StringBuilder path = new StringBuilder();
			path.append(getUsshome());
			path.append(SLASH_SYBMOL);
			path.append("etc");
			path.append(SLASH_SYBMOL);
			path.append("wlp");
			path.append(SLASH_SYBMOL);
			path.append("extensions");
			path.append(SLASH_SYBMOL);
			path.append("cicsts");
			path.append(SLASH_SYBMOL);
			path.append("templates");
			path.append(SLASH_SYBMOL);
			path.append("servers");
			path.append(SLASH_SYBMOL);
			path.append(libertyServer.getServerName());
			path.append(SLASH_SYBMOL);
			path.append("server.xml");
			IZosUNIXFile cicsSuppliedserverXml = this.zosFileHandler.newUNIXFile(path.toString(), this.cicsZosImage);
			cicsSuppliedserverXml.setDataType(UNIXFileDataType.BINARY);
			String content = cicsSuppliedserverXml.retrieve();
			libertyServer.getServerXml().setServerXmlFromString(content);
		} catch (ZosLibertyServerException | ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Unable to create new zOS Liberty Server for JVMSERVER " + this.getName(), e);
		}
		return libertyServer;
	}

	protected IZosLiberty getLiberty() throws CicsJvmserverResourceException {
		if (this.zosLiberty == null) { 
			try {
				this.zosLiberty = cicsResourceManager.getZosLiberty();
			} catch (CicsResourceManagerException e) {
				throw new CicsJvmserverResourceException("Unable to get zOS Liberty", e);
			}
		}
		return this.zosLiberty;
	}

	protected String absolutePath(String path) throws CicsJvmserverResourceException {
		if (!path.startsWith(SLASH_SYBMOL)) {
			path = getHomeDirectory() + path;
		}
		if (!path.endsWith(SLASH_SYBMOL)) {
			path = path + SLASH_SYBMOL;
		}
		return path;
	}

	protected String getDefaultWorkingDirectoryValue() throws CicsJvmserverResourceException {
		if (this.defaultWorkingDirectoryValue == null) {
			try {
				this.defaultWorkingDirectoryValue = this.cicsZosImage.getRunTemporaryUNIXPath() + SLASH_SYBMOL + "";
			} catch (ZosManagerException e) {
				throw new CicsJvmserverResourceException("Unable to get the run temporary UNIX directory for image " + this.cicsZosImage.getImageID(), e);
			}
		}
		return this.defaultWorkingDirectoryValue;
	}

	protected IZosUNIXFile getDefaultLogsDiretory() throws CicsJvmserverResourceException {
		if (this.logsDirectory == null) {
			try {
				this.logsDirectory = this.zosFileHandler.newUNIXFile(getDefaultWorkingDirectoryValue() + SLASH_SYBMOL + this.getApplid() + SLASH_SYBMOL + getName() + SLASH_SYBMOL, cicsZosImage);
			} catch (CicsJvmserverResourceException | ZosUNIXFileException e) {
				throw new CicsJvmserverResourceException("Unable to get default logs directory", e);
			}
		}
		return this.logsDirectory;
	}

	protected NavigableMap<String, IZosUNIXFile> decendingDirectoryList(IZosUNIXFile directory) throws CicsJvmserverResourceException {
		try {
			return ((TreeMap<String, IZosUNIXFile>) directory.directoryList()).descendingMap();
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Unable to list directory", e);
		}
	}

	//TODO: get from property
	protected String getDefaultJavaHomeValue() {
		if (this.defaultJavaHomeValue == null) {
			this.defaultJavaHomeValue = "/java/java80_64/J8.0_64";
		}
		return this.defaultJavaHomeValue;
	}

	//TODO: get from property
	protected String getDefaultWlpInstallDirValue() {
		if (this.defaultWlpInstallDirValue == null) {
			this.defaultWlpInstallDirValue = "&USSHOME;/wlp";
		}
		return this.defaultWlpInstallDirValue;
	}

	//TODO: get from property
	protected String getDefaultWlpUserDirValue() throws CicsJvmserverResourceException {
		if (this.defaultWlpUserDirValue == null) {
			this.defaultWlpUserDirValue =  getDefaultWorkingDirectoryValue() + SLASH_SYBMOL + "./&APPLID;/&JVMSERVER;/wlp/usr";
		}
		return this.defaultWlpUserDirValue;
	}

	protected IJvmprofile newJvmprofileFromCicsSuppliedProfile(String name) throws CicsJvmserverResourceException {
		String cicsSuppliedProfileFileName = this.jvmserverType.getCicsSuppliedProfile();
		if (this.jvmserverType == JvmserverType.UNKNOWN) {
			throw new CicsJvmserverResourceException("Unknown JVM server type " + jvmserverType);
		}
		IZosUNIXFile cicsSuppliedProfile = getCicsSuppliedProfile(cicsSuppliedProfileFileName);
		try {
			cicsSuppliedProfile.setDataType(UNIXFileDataType.TEXT);
			String content = cicsSuppliedProfile.retrieve();
			return new JvmprofileImpl(this.zosFileHandler, this.cicsZosImage, name, content);
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Problem creating JVM profile from template " + cicsSuppliedProfile.getUnixPath(), e);
		}
	}

	protected IZosUNIXFile getCicsSuppliedProfile(String cicsSuppliedProfileFileName) throws CicsJvmserverResourceException {
		String templatePath = getUsshome() + SLASH_SYBMOL + "JVMProfiles" + SLASH_SYBMOL + cicsSuppliedProfileFileName;
		IZosUNIXFile cicsSuppliedProfile;
		try {
			cicsSuppliedProfile = this.zosFileHandler.newUNIXFile(templatePath, this.cicsZosImage);
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Unable to get CICS supplied JVM profile " +  templatePath, e);
		}
		return cicsSuppliedProfile;
	}

	protected JvmserverType determineJvmserverType() {
		if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_CLASSPATH_PREFIX)) {
			return JvmserverType.LIBERTY;
		} else if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_SECURITY_TOKEN_SERVICE)) {
			return JvmserverType.STS;
		} else if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_JAVA_PIPELINE)) {
			return JvmserverType.AXIS2;
		} else if (this.jvmprofile.containsOption(JvmprofileImpl.OPTION_CLASSPATH_PREFIX) || this.jvmprofile.containsOption(JvmprofileImpl.OPTION_CLASSPATH_SUFFIX)) {
			return JvmserverType.CLASSPATH;
		} else if (JvmserverType.CMCI.getCicsSuppliedProfile().startsWith(this.jvmprofile.getProfileName())) {
			return JvmserverType.CMCI;
		} else  if (this.jvmprofile.getProfileMap().isEmpty()) {
			return JvmserverType.OSGI;
		}
		return JvmserverType.UNKNOWN;
	}

	protected String getHomeDirectory() throws CicsJvmserverResourceException {
		if (this.cicsRegionHomeDirectory == null) {
			this.cicsRegionHomeDirectory = SLASH_SYBMOL + "u" + SLASH_SYBMOL + getCicsRegionUserid().toLowerCase() + SLASH_SYBMOL;
		}
		return this.cicsRegionHomeDirectory;
	}

	protected String getCicsZosImageDefaultCredentialsUserid() throws CicsJvmserverResourceException {
		if (this.cicsZosImageDefaultCredentialsUserid == null) {
		    this.cicsZosImageDefaultCredentialsUserid = ((ICredentialsUsernamePassword) getCicsZosImageDefaultCredentials()).getUsername();
		}
		return this.cicsZosImageDefaultCredentialsUserid;
	}


	protected ICredentials getCicsZosImageDefaultCredentials() throws CicsJvmserverResourceException {
		if (this.cicsZosImageDefaultCredentials == null) {
			try {
				this.cicsZosImageDefaultCredentials = this.cicsZosImage.getDefaultCredentials();
			} catch (ZosManagerException e) {
				throw new CicsJvmserverResourceException("Problem getting CICS region zOS image default credentials", e);
			}
		}
		return this.cicsZosImageDefaultCredentials;
	}

	@Override
	public void setDefinitionDescriptionAttribute(String value) {
		this.resourceDefinitionDescription = value;
	}

	@Override
	public void setDefinitionStatusAttribute(CicsResourceStatus value) {
		this.resourceDefinitionStatus = value;
	}

	@Override
	public void setResourceDefinitionLerunopts(String lerunopts) {
		this.resourceDefinitionLerunopts = lerunopts;
	}

	@Override
	public void setResourceDefinitionThreadlimit(int threadlimit) {
		this.resourceDefinitionThreadlimit = threadlimit;
	}

	@Override
	public String getResourceDefinitionNameAttribute() {
		return this.resourceDefinitionName;
	}

	@Override
	public String getResourceDefinitionGroupAttribute() {
		return this.resourceDefinitionGroup;
	}

	@Override
	public String getResourceDefinitionDescriptionAttribute() {
		return this.resourceDefinitionDescription;
	}

	@Override
	public CicsResourceStatus getResourceDefinitionStatusAttribute() {
		return this.resourceDefinitionStatus;
	}

	@Override
	public CicsResourceStatus getResourceDefinitionStatus() {
		return this.resourceDefinitionStatus;
	}

	@Override
	public String getResourceDefinitionJvmprofile() {
		return this.resourceDefinitionJvmprofile;
	}

	@Override
	public String getResourceDefinitionLerunopts() {
		return this.resourceDefinitionLerunopts;
	}

	@Override
	public int getResourceDefinitionThreadlimit() {
		return this.resourceDefinitionThreadlimit;
	}

	@Override
	public void buildResourceDefinition() throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.ceda().createResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), this.resourceDefinitionGroup, buildResourceParameters());
			//TODO Check create was successful 
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Unable to build JVMSERVER resource definition", e);
		}
	}

	@Override
	public void buildInstallResourceDefinition() throws CicsJvmserverResourceException {
		buildResourceDefinition();
		installResourceDefinition();
	}

	@Override
	public void installResourceDefinition() throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.ceda().installResource(cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), this.resourceDefinitionGroup);
			//TODO Check install was successful 
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Unable to install JVMSERVER resource definition", e);
		}
	}

	@Override
	public void enable() throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.cemt().setResource(cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), "ENABLED");
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Problem enabling JVMSERVER " + getName(), e);
		}
	}

	@Override
	public boolean waitForEnable() throws CicsJvmserverResourceException {
		return waitForEnable(getDefaultTimeout());
	}

	@Override
	public boolean waitForEnable(int millisecondTimeout) throws CicsJvmserverResourceException {
		int timeout = millisecondTimeout/1000;
		for (int i = 0; i < timeout; i++) {
			if (isEnabled()) {
				return true;
			}
		}
		return isEnabled();
	}

	@Override
	public boolean isEnabled() throws CicsJvmserverResourceException {
		CicstsHashMap cemtMap = cemtInquire();
		if (cemtMap == null) {
			return false;
		}
		return cemtInquire().isParameterEquals("enablestatus", CicsResourceStatus.ENABLED.toString());
	}

	@Override
	public boolean disable() throws CicsJvmserverResourceException {
		return disable(PurgeType.PHASEOUT, getDefaultTimeout());
	}

	@Override
	public boolean disable(PurgeType purgeType) throws CicsJvmserverResourceException {
		return disable(purgeType, getDefaultTimeout());
	}

	@Override
	public boolean disable(PurgeType purgeType, int millisecondTimeout) throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.cemt().setResource(cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), "DISABLED " + purgeType);
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Problem disabling JVMSERVER " + getName(), e);
		}
		return waitForDisable(millisecondTimeout);
	}

	@Override
	public PurgeType disableWithEscalate() throws CicsJvmserverResourceException {
		return disableWithEscalate(getDefaultTimeout());
	}

	@Override
	public PurgeType disableWithEscalate(int stepMillisecondTimeout) throws CicsJvmserverResourceException {
		PurgeType purgeType = PurgeType.PHASEOUT;
		if (!disable(purgeType, stepMillisecondTimeout)) {
			purgeType = PurgeType.PURGE;
			if (!disable(purgeType, stepMillisecondTimeout)) {
				purgeType = PurgeType.FORCEPURGE;
				if (!disable(purgeType, stepMillisecondTimeout)) {
					purgeType = PurgeType.KILL;
					if (!disable(purgeType, stepMillisecondTimeout)) {
						purgeType = PurgeType.FAILED;
					}
				}
			}
		}
		
		return purgeType;
	}

	@Override
	public boolean waitForDisable() throws CicsJvmserverResourceException {
		return waitForDisable(getDefaultTimeout());
	}

	@Override
	public boolean waitForDisable(int millisecondTimeout) throws CicsJvmserverResourceException {
		int timeout = millisecondTimeout/1000;
		for (int i = 0; i < timeout; i++) {
			if (!isEnabled()) {
				return true;
			}
		}
		if (isEnabled()) {
			throw new CicsJvmserverResourceException("JVMSERVER " + getName() + " not disabled in " + millisecondTimeout + "ms");
		}
		return true;
	}

	@Override
	public void delete() throws CicsJvmserverResourceException {
		delete(false);
	}

	@Override
	public void delete(boolean ignoreErrors) throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.ceda().deleteResource(cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), resourceDefinitionGroup);
		} catch (CicstsManagerException e) {
			String message = "Problem deleteing JVMSERVER " + getName();
			if (ignoreErrors) {
				logger.warn(message + " - " + e.getMessage());
			} else {
				throw new CicsJvmserverResourceException(message, e);
			}
		}
		try {
			this.jvmprofile.delete();
		} catch (CicstsManagerException e) {
			String message = "Problem deleteing JVM profile for JVMSERVER " + getName();
			if (ignoreErrors) {
				logger.warn(message + " - " + e.getMessage());
			} else {
				throw new CicsJvmserverResourceException(message, e);
			}
		}
		try {
			deleteJvmserverLogs();
		} catch (CicstsManagerException e) {
			String message = "Problem deleteing logs for JVMSERVER " + getName();
			if (ignoreErrors) {
				logger.warn(message + " - " + e.getMessage());
			} else {
				throw new CicsJvmserverResourceException(message, e);
			}
		}
	}

	@Override
	public void discard() throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.cemt().discardResource(cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName());
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Problem discarding JVMSERVER " + getName(), e);
		}
	}

	@Override
	public void disableDiscardDelete(boolean ignoreErrors) throws CicsJvmserverResourceException {
		disable();
		discard();
		delete();
	}

	@Override
	public void setThreadLimit(int threadlimit) throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName(), "THREADLIMIT(" + threadlimit + ")");
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Problem setting THREADLIMIT for JVMSERVER " + getName(), e);
		}
	}

	@Override
	public int getThreadLimit() throws CicsJvmserverResourceException {
		CicstsHashMap cemtMap;
		try {
			 cemtMap = cemtInquire();
		} catch (CicsJvmserverResourceException e) {
			throw new CicsJvmserverResourceException("Problem getting THREADLIMIT for JVMSERVER " + getName(), e);
		}
		return Integer.valueOf(cemtMap.get("Threadlimit"));
	}

	@Override
	public int getThreadCount() throws CicsJvmserverResourceException {
		CicstsHashMap cemtMap;
		try {
			 cemtMap = cemtInquire();
		} catch (CicsJvmserverResourceException e) {
			throw new CicsJvmserverResourceException("Problem getting THREADCOUNT for JVMSERVER " + getName(), e);
		}
		return Integer.valueOf(cemtMap.get("Threadcount"));
	}

	@Override
	public void build() throws CicsJvmserverResourceException {
		try {
			this.jvmprofile.build();
			if (isLiberty()) {
				this.zosLibertyServer.getServerXml().build();
			}
			buildResourceDefinition();
			installResourceDefinition();
		} catch (CicsJvmserverResourceException | ZosLibertyServerException e) {
			throw new CicsJvmserverResourceException("Problem building JVM server " + getName(), e);
		}
	}

	@Override
	public IJvmprofile getJvmprofile() {
		return this.jvmprofile;
	}

	@Override
	public void buildProfile() throws CicsJvmserverResourceException {
		this.jvmprofile.build();
	}

	@Override
	public boolean isProfileBuilt() {
		return false;
	}

	@Override
	public void setLibertyServer(IZosLibertyServer zosLibertyServer) {
		this.zosLibertyServer = zosLibertyServer;
	}

	@Override
	public String getName() {
		return this.resourceDefinitionName;
	}

	@Override
	public IZosLibertyServer getLibertyServer() {
		return this.zosLibertyServer;
	}

	@Override
	public IZosUNIXFile getJavaHome() throws CicsJvmserverResourceException {
		if (this.javaHome == null) {
			String javaHomeValue = this.jvmprofile.getProfileValue(OPTION_JAVA_HOME);
			if (javaHomeValue == null) {
				javaHomeValue = getDefaultJavaHomeValue();
				this.jvmprofile.setProfileValue(OPTION_JAVA_HOME, javaHomeValue); 
			}
			try {
				this.javaHome = this.zosFileHandler.newUNIXFile(javaHomeValue, this.cicsZosImage);
			} catch (ZosUNIXFileException e) {
				throw new CicsJvmserverResourceException("Problem getting Java home", e);
			}
		}
		return this.javaHome;
	}

	@Override
	public IZosUNIXFile getWorkingDirectory() throws CicsJvmserverResourceException {
		if (this.workingDirectory == null) {
			String workDirValue = this.jvmprofile.getProfileValue(OPTION_WORK_DIR);
			if (workDirValue == null) {
				workDirValue = getDefaultWorkingDirectoryValue();
				this.jvmprofile.setProfileValue(OPTION_WORK_DIR, workDirValue); 
			}
			if (!workDirValue.startsWith(SLASH_SYBMOL)) {
				workDirValue = getHomeDirectory() + workDirValue;
			}
			try {
				this.workingDirectory = this.zosFileHandler.newUNIXFile(getHomeDirectory() + workDirValue, this.cicsZosImage);
			} catch (ZosUNIXFileException e) {
				throw new CicsJvmserverResourceException("Problem getting working directory", e);
			}
		}
		return this.workingDirectory;
	}

	@Override
	public IJvmserverLog getJvmLog() throws CicsJvmserverResourceException {
		//TODO: JVM restarted?
		if (this.jvmLogLog == null) {
			this.jvmLogLog = getLog(LOG_CEMT_PARAMETER, LOG_PROFILE_OPTION, LOG_FILE_SUFFIX);
		}
		return this.jvmLogLog;
	}

	@Override
	public IJvmserverLog getStdOut() throws CicsJvmserverResourceException {
		//TODO: JVM restarted?
		if (this.stdOutLog == null) {
			this.stdOutLog = getLog(STDOUT_CEMT_PARAMETER, STDOUT_PROFILE_OPTION, STDOUT_FILE_SUFFIX);
		}
		return this.stdOutLog;
	}

	@Override
	public IJvmserverLog getStdErr() throws CicsJvmserverResourceException {
		//TODO: JVM restarted?
		if (this.stdErrLog == null) {
			this.stdErrLog = getLog(STDERR_CEMT_PARAMETER, STDERR_PROFILE_OPTION, STDERR_FILE_SUFFIX);
		}
		return this.stdErrLog;
	}

	@Override
	public IJvmserverLog getJvmTrace() throws CicsJvmserverResourceException {
		//TODO: JVM restarted?
		if (this.jvmTraceLog == null) {
			this.jvmTraceLog = getLog(TRACE_CEMT_PARAMETER, TRACE_PROFILE_OPTION, TRACE_FILE_SUFFIX);
		}
		return this.jvmTraceLog;
	}

	@Override
	public void checkpointLogs() throws CicsJvmserverResourceException {	
		if (this.jvmLogLog != null) {
			this.jvmLogLog.checkpoint();
		}
		if (this.stdOutLog != null) {
			this.stdOutLog.checkpoint();
		}
		if (this.stdErrLog != null) {
			this.stdErrLog.checkpoint();
		}
		if (this.jvmTraceLog != null) {
			this.jvmTraceLog.checkpoint();
		}
	}

	@Override
	public void saveToResultsArchive() throws CicsJvmserverResourceException {
		saveToResultsArchive(getDefaultRasPath());
	}

	@Override
	public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException {	
		if (this.jvmprofile != null) {
			this.jvmprofile.saveToResultsArchive(rasPath);
		}	
		if (this.jvmLogLog != null) {
			this.jvmLogLog.saveToResultsArchive(rasPath);
		}
		if (this.stdOutLog != null) {
			this.stdOutLog.saveToResultsArchive(rasPath);
		}
		if (this.stdErrLog != null) {
			this.stdErrLog.saveToResultsArchive(rasPath);
		}
		if (this.jvmTraceLog != null) {
			this.jvmTraceLog.saveToResultsArchive(rasPath);
		}
		saveDiagnosticsToResultsArchive(rasPath);
		if (isLiberty()) {
			this.zosLiberty.saveToResultsArchive(rasPath);
		}
	}

	@Override
	public void deleteJvmserverLogs() throws CicsJvmserverResourceException {
		if (this.jvmLogLog != null) {
			this.jvmLogLog.delete();
		}
		if (this.stdOutLog != null) {
			this.stdOutLog.delete();
		}
		if (this.stdErrLog != null) {
			this.stdErrLog.delete();
		}
		if (this.jvmTraceLog != null) {
			this.jvmTraceLog.delete();
		}
		if (isLiberty()) {
			this.zosLiberty.deleteLogs();
		}
	}

	@Override
	public void clearJvmLogs() throws CicsJvmserverResourceException {		
		String rasPath = getApplid() + SLASH + getName() + SLASH;
		clearJvmLogs(rasPath);
	}

	@Override
	public void clearJvmLogs(String rasPath) throws CicsJvmserverResourceException {
		saveToResultsArchive();
		deleteJvmserverLogs();
	}
	
	@Override
	public String toString() {
		return "[JVM server] " + getName();
	}

	protected String getDefaultRasPath() {
		return this.cicsResourceManager.getCurrentTestMethodArchiveFolder().toString() + SLASH + getApplid() + SLASH + getName() + SLASH;
	}

	// TODO: get from property
	protected int getDefaultTimeout() {
		if (this.defaultTimout == -1) {
			this.defaultTimout = 10000;
		}
		return this.defaultTimout ;
	}

	protected IJvmserverLog getLog(String cemtParameter, String jvmprofileOption, String fileSuffix) throws CicsJvmserverResourceException {
		String logName = getLogName(cemtParameter, jvmprofileOption, fileSuffix);
		try {
			if (isDdname(logName)) {
				return new JvmserverLogImpl(getCicsRegionJob().getSpoolFile(getDdname(logName)), newLogScanner());
			} else {
				return new JvmserverLogImpl(this.zosFileHandler.newUNIXFile(logName, this.cicsZosImage), newLogScanner());
			}
		} catch (ZosBatchException | ZosUNIXFileException | CicsResourceManagerException e) {
			throw new CicsJvmserverResourceException("Problem creating IJvmserverLog object for " + cemtParameter, e);
		}
		
	}

	protected String getDdname(String logName) {
		return logName.substring(5);
	}

	protected String getLogName(String cemtParameter, String jvmprofileOption, String fileSuffix) throws CicsJvmserverResourceException {
		// First try CEMT
		CicstsHashMap cemtMap = cemtInquire();
		if (cemtMap != null && cemtMap.containsKey(cemtParameter)) {
			return cemtMap.get(cemtParameter);
		}
	
		// Try jvmprofile
		if (this.jvmprofile.containsOption(jvmprofileOption)) {
			String value = this.jvmprofile.getProfileValue(jvmprofileOption);
			if (value != null) {
				String parsedValue = parseJvmprofileSymbols(value);
				if (!parsedValue .contains(SYMBOL_DATE) && !parsedValue.contains(SYMBOL_TIME)) {
					return parsedValue;
				}
			}
		}
			
		// Look on the file system
		for(Map.Entry<String, IZosUNIXFile> entry : decendingDirectoryList(getDefaultLogsDiretory()).entrySet()) {
			if (entry.getKey().endsWith("." + fileSuffix)) {
				return entry.getValue().getUnixPath();
			}
		}
		
		throw new CicsJvmserverResourceException("Unable to establish JVM server " + cemtParameter + " log file");
		    
	}
	
	protected boolean isDdname(String logName) {
		return logName.startsWith("//DD:");
	}

	protected void getSystemValues() throws CicsJvmserverResourceException {
		try {
			this.cicsRegion.ceci().startCECISession(this.cicsTerminal);
			this.cicsRegion.ceci().issueCommand(this.cicsTerminal, "INQUIRE SYSTEM JOBNAME(&JOBNAME)", false).getResponseOutputValues();
			this.cicsRegionJobname = this.cicsRegion.ceci().retrieveVariableText(this.cicsTerminal, "&JOBNAME");
			this.cicsRegion.ceci().issueCommand(this.cicsTerminal, "INQUIRE SYSTEM REGIONUSERID(&USER)", false).getResponseOutputValues();
			this.cicsRegionUserid = this.cicsRegion.ceci().retrieveVariableText(this.cicsTerminal, "&USER");
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Unable to retrieve CICS region system values", e);
		}
	}

	protected IZosBatchJob getCicsRegionJob() throws CicsJvmserverResourceException {
		if (this.cicsRegionJob == null) {
			try { 
				//TODO: Get from properties?
				List<IZosBatchJob> jobs = this.cicsResourceManager.getBatch(this.cicsZosImage).getJobs(getCicsJobname(), getCicsRegionUserid());
				for (IZosBatchJob job : jobs) {
					if (job.getStatus().equals(JobStatus.ACTIVE)) {
						String jesmsglg = job.getSpoolFile("JESMSGLG").getRecords();
						Pattern pattern = Pattern.compile("DFHSI1517\\s(\\w+)");
				    	Matcher matcher = pattern.matcher(jesmsglg);
				    	if (matcher.find() && matcher.groupCount() == 1 && getApplid().equals(matcher.group(1))) {
			    			this.cicsRegionJob = job; //TODO Return here and what if null
			    			break;
				    	}
					}
				}
			} catch (CicstsManagerException | ZosBatchException e) {
				throw new CicsJvmserverResourceException("Unable to get CICS job", e);
			}
		}
		return this.cicsRegionJob;
	}

	protected String getCicsJobname() throws CicsJvmserverResourceException {
		if (this.cicsRegionJobname == null) {
			getSystemValues();
		}
		return this.cicsRegionJobname;
	}

	protected String getCicsRegionUserid() throws CicsJvmserverResourceException {
		if (this.cicsRegionUserid == null) {
			getSystemValues();
		}
		return this.cicsRegionUserid;
	}

	protected String buildResourceParameters() {
		StringBuilder resourceParameters = new StringBuilder();
		appendNotNull(resourceParameters, "DESCRIPTION", getResourceDefinitionDescriptionAttribute());
		appendNotNull(resourceParameters, "STATUS", getResourceDefinitionStatusAttribute().toString());
		appendNotNull(resourceParameters, "JVMPROFILE", getResourceDefinitionJvmprofile());
		appendNotNull(resourceParameters, "LERUNOPTS", getResourceDefinitionLerunopts());
		appendNotNull(resourceParameters, "THREADLIMIT", Integer.toString(getResourceDefinitionThreadlimit()));
		for (Map.Entry<String, String> entry : this.resourceDefinitionAttribute.entrySet()) {
			appendNotNull(resourceParameters, entry.getKey(), entry.getValue());
		}
		return resourceParameters.toString();
	}

	protected StringBuilder appendNotNull(StringBuilder resourceParameters, String attribute, String value) {
		if (value != null && !value.isEmpty()) {
			resourceParameters.append(attribute);
			resourceParameters.append(" (");
			resourceParameters.append(value);
			resourceParameters.append(") ");
		}
		return resourceParameters;
	}

	protected CicstsHashMap cemtInquire() throws CicsJvmserverResourceException {
		CicstsHashMap cemtMap;
		try {
			cemtMap = this.cicsRegion.cemt().inquireResource(this.cicsTerminal, RESOURCE_TYPE_JVMSERVER, getName());
		} catch (CicstsManagerException e) {
			throw new CicsJvmserverResourceException("Problem inquiring JVMSERVER " + getName(), e);
		}
		return cemtMap;
	}
	
	protected String parseJvmprofileSymbols(String value) throws CicsJvmserverResourceException {
		if (value.contains(SYMBOL_DATE) || value.contains(SYMBOL_TIME)) {
			throw new CicsJvmserverResourceException("JVM profile options \"" + SYMBOL_DATE + "\" and \"" + SYMBOL_TIME + "\" not supported by JVM server Manager");
		}
		String parsedValue = StringUtils.replace(value, SYMBOL_APPLID, getApplid());
		parsedValue = StringUtils.replace(parsedValue, SYMBOL_USSHOME, getUsshome());
		parsedValue = StringUtils.replace(parsedValue, SYMBOL_CONFIGROOT, getUsshome());
		parsedValue = StringUtils.replace(parsedValue, SYMBOL_JVMSERVER, getName());
		parsedValue = StringUtils.replace(parsedValue, SYMBOL_USSHOME, getUsshome());
		
		return parsedValue;
	}
	
	protected String getUsshome() throws CicsJvmserverResourceException {
		if (this.cicsUsshome == null) {
			try {
				this.cicsUsshome = this.cicsRegion.getUssHome();
			} catch (CicstsManagerException e) {
				throw new CicsJvmserverResourceException("Unable to get value of USSHOME", e);
			}
		}
		return this.cicsUsshome;
	}
	
	protected String getApplid() {
		if (this.cicsApplid == null) {
			this.cicsApplid = this.cicsRegion.getApplid();
		}
		return this.cicsApplid;
	}
	
	protected String getJvmProfileDir() throws CicsJvmserverResourceException {
		if (this.cicsJvmprofileDir == null) {
			try {
				this.cicsJvmprofileDir = this.cicsRegion.getJvmProfileDir();
			} catch (CicstsManagerException e) {
				throw new CicsJvmserverResourceException("Unable to get value of JVMPROFILEDIR", e);
			}
		}
		return this.cicsJvmprofileDir;
	}
	
	protected String getConfigRoot() throws CicsJvmserverResourceException {
		if (this.cicsConfigroot == null) {
			//TOODO: get CONFIGROOT
			this.cicsConfigroot = getJvmProfileDir();
		}
		return this.cicsConfigroot;
	}
	
	protected IZosUNIXFile getDiagnosticsDirectory() throws CicsJvmserverResourceException {
		if (this.diagnosticsDirectory == null) {
			try {
				this.diagnosticsDirectory = this.zosFileHandler.newUNIXFile(getWorkingDirectory() + SLASH + "diagnostics" + SLASH + getApplid() + SLASH + getName() + SLASH, cicsZosImage);
			} catch (ZosUNIXFileException | CicsJvmserverResourceException e) {
				throw new CicsJvmserverResourceException("Unable to get diagnostics directory", e);
			}
		}
		return this.diagnosticsDirectory;
	}

	protected void saveDiagnosticsToResultsArchive(String rasPath) throws CicsJvmserverResourceException {
		try {
			String diagnostics = "diagnostics";
			if (!rasPath.endsWith(SLASH)) {
				diagnostics = SLASH + diagnostics;
			}
			getDiagnosticsDirectory().saveToResultsArchive(rasPath + diagnostics);
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Unable to archive diagnostics directory", e);
		}
	}
}
