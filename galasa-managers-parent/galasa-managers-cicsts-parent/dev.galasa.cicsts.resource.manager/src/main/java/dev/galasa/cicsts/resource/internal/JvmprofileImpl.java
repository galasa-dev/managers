package dev.galasa.cicsts.resource.internal;

import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import dev.galasa.cicsts.cicsresource.CicsJvmprofileResourceException;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.IJvmprofile;
import dev.galasa.cicsts.cicsresource.IJvmserver.JvmserverType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServer;

public class JvmprofileImpl implements IJvmprofile {
	
	private static final Log logger = LogFactory.getLog(JvmprofileImpl.class);

	private IZosFileHandler zosFileHandler;
	private IZosImage zosImage;
	
	private IZosUNIXFile profileUnixFile;
	private String profileName;
	private HashMap<String, String> profileMap;
	private String jvmProfileDir;
	private List<IZosUNIXFile> profileIncludes = new ArrayList<>();

	private static final String COMMA_SYBMOL = ",";
	private static final String HASH_SYBMOL = "#";
	private static final String PLUS_SYBMOL = "+";
	private static final String MINUS_SYBMOL = "-";
	private static final String EQUALS_SYBMOL = "=";
	private static final String COLON_SYBMOL = ":";
	private static final String BACK_SLASH_SYBMOL = "\\";
	private static final String SLASH_SYBMOL = "/";
	private static final String NEW_LINE = "\n";
	private static final String GCTHREADS = "-Xgcthreads";
	private static final String XMS = "-Xms";
	private static final String XMSO = "-Xmso";
	private static final String XMX = "-Xmx";
	private static final String XSCMX = "-Xscmx";
	private static final String MINUS_D = "-D";
	
	protected static final String OPTION_CLASSPATH_PREFIX = "CLASSPATH_PREFIX";
	protected static final String OPTION_CLASSPATH_SUFFIX = "CLASSPATH_SUFFIX";
	protected static final String OPTION_JAVA_PIPELINE = "JAVA_PIPELINE";
	protected static final String OPTION_PC_INCLUDE = "%INCLUDE";
	protected static final String OPTION_SECURITY_TOKEN_SERVICE = "SECURITY_TOKEN_SERVICE";
	protected static final String OPTION_WLP_INSTALL_DIR = "WLP_INSTALL_DIR";

	public JvmprofileImpl(IZosFileHandler zosFileHandler, IZosImage zosImage, String jvmprofileName) {
		this.zosFileHandler = zosFileHandler;
		this.zosImage = zosImage;
		this.profileName = jvmprofileName;
		this.profileMap = new HashMap<>();
	}

	public JvmprofileImpl(IZosFileHandler zosFileHandler, IZosImage zosImage, String jvmprofileName, HashMap<String, String> content) {
		this.zosFileHandler = zosFileHandler;
		this.zosImage = zosImage;
		this.profileName = jvmprofileName;
		this.profileMap = content;
	}

	public JvmprofileImpl(IZosFileHandler zosFileHandler, IZosImage zosImage, String jvmprofileName, String content) {
		this.zosFileHandler = zosFileHandler;
		this.zosImage = zosImage;
		this.profileName = jvmprofileName;
		parseContent(content);
	}

	public JvmprofileImpl(IZosFileHandler zosFileHandler, IZosImage zosImage, String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmprofileResourceException {
		//TODO
		this.zosFileHandler = zosFileHandler;
		this.zosImage = zosImage;
		this.profileName = jvmprofileName;
		switch (jvmserverType) {
		case OSGI:
			//
			break;

		default:
			break;
		}
		
		if (jvmserverType == JvmserverType.OSGI) {
			// get USSHOME/JVMProfiles/DFHOSGI.jvmprofile
			// parse profile
			// update where required
			// save profile JVMPROFILEDIR/name.jvmprofile
		}
		// TODO Auto-generated constructor stub
	}

	protected void parseContent(String content) {
    	String continuationKey = null;
    	this.profileMap = new LinkedHashMap<>();
		String[] contentArray = content.split("\\r?\\n");
		for (int i = 0; i < contentArray.length; i++) {
			String line = contentArray[i];
			List<String> option = parseLine(line);
			if (!option.isEmpty()) {
				String optionKey = option.get(0);
				String optionValue = option.size() >= 2? option.get(1): null;
				if (optionKey.startsWith(PLUS_SYBMOL)) {
					optionKey = optionKey.substring(1);
					if (profileMap.containsKey(optionKey) && optionValue != null) {
						this.profileMap.put(optionKey, this.profileMap.get(optionKey) + COMMA_SYBMOL + optionValue);
					} else {
						this.profileMap.put(optionKey, optionValue);
					}
				} else if (optionKey.equals(OPTION_PC_INCLUDE)) {
					if (profileMap.containsKey(optionKey) && optionValue != null) {
						this.profileMap.put(optionKey, this.profileMap.get(optionKey) + COMMA_SYBMOL + optionValue);
					} else {
						this.profileMap.put(optionKey, optionValue);
					}
				} else {
					if (continuationKey != null) {
						optionValue = this.profileMap.get(continuationKey) + optionKey;
						optionKey = continuationKey;
					}
					if (optionValue != null && optionValue.endsWith(BACK_SLASH_SYBMOL)) {
						continuationKey = optionKey;
						optionValue = optionValue.substring(0, optionValue.length()-1);
					} else {
						continuationKey = null;
					}
					this.profileMap.put(optionKey, optionValue);
				}
			}
		}
	}

	protected List<String> parseLine(String line) {
		List<String> option = new ArrayList<>();
		line = line.trim();
		if (!line.startsWith(HASH_SYBMOL) && !line.isEmpty()) {
			if (line.startsWith(XMSO)) {
				option.add(line.substring(0, XMSO.length()));
				option.add(line.substring(XMSO.length()));
			} else if (line.startsWith(XMS)) {
				option.add(line.substring(0, XMS.length()));
				option.add(line.substring(XMS.length()));
			} else if (line.startsWith(XMX)) {
				option.add(line.substring(0, XMX.length()));
				option.add(line.substring(XMX.length()));
			} else if (line.startsWith(XSCMX)) {
				option.add(line.substring(0, XSCMX.length()));
				option.add(line.substring(XSCMX.length()));
			} else if (line.startsWith(GCTHREADS)) {
				option.add(line.substring(0, GCTHREADS.length()));
				option.add(line.substring(GCTHREADS.length()));
			} else if (line.startsWith(MINUS_SYBMOL) && !line.startsWith(MINUS_D)) {
				if (line.contains(COLON_SYBMOL)) {
					String[] lineArray = line.split(COLON_SYBMOL);
					if (lineArray.length >= 2) {
						option.add(lineArray[0] + COLON_SYBMOL);
						StringBuilder value = new StringBuilder();
						for (int i = 1; i < lineArray.length; i++) {
							value.append(lineArray[1]);
							value.append(COLON_SYBMOL);
						}
						if (!line.endsWith(COLON_SYBMOL)) {
							value.delete(value.length()-1, value.length());
						}
						option.add(value.toString());
					} 
				}				
			} else {
				String[] lineArray = line.split(EQUALS_SYBMOL);
				option.add(lineArray[0]);
				if (lineArray.length >= 2) {
					option.add(lineArray[1]);
				}
			}
		}
		
		// Strip trailing comments
		if (option.size() == 2) {
			String optionValue = option.get(1);
			int posComment = StringUtils.ordinalIndexOf(optionValue, " #", 1);
			int posQuote1 = StringUtils.ordinalIndexOf(optionValue, "\"", 1);
			int posQuote2 = StringUtils.ordinalIndexOf(optionValue, "\"", 2);
			if ((posComment > -1 && posQuote1 < 0) || (posComment > -1 && posQuote1  < posComment && posQuote2 > -1)) {
				option.set(1, optionValue.substring(0, posComment).trim());
			}
		}
		return option;
	}


	@Override
	public void setProfileName(String name) {
		this.profileName = name;
	}

	@Override
	public void setJvmProfileDir(String jvmProfileDir) {
		this.jvmProfileDir = jvmProfileDir;
	}

	@Override
	public IZosUNIXFile getProfile() {
		return this.profileUnixFile;
	}

	@Override
	public String getProfileName() {
		return this.profileName;
	}

	@Override
	public void setProfileValue(String key, String value) {
		this.profileMap.put(key, value);
	}

	@Override
	public void appendProfileValue(String key, String value) {
		this.profileMap.put(key, getProfileValue(key) + COMMA_SYBMOL + value);
	}

	@Override
	public String getProfileValue(String key) {
		return this.profileMap.get(key);
	}

	@Override
	public void removeProfileValue(String key) {
		this.profileMap.remove(key);

	}

	@Override
	public boolean containsOption(String option) {
		return this.profileMap.containsKey(option);
	}

	@Override
	public HashMap<String, String> getProfileMap() {
		return this.profileMap;
	}

	@Override
	public String getProfileString() {
    	StringBuilder content = new StringBuilder();
    	for (Entry<String, String> entry : this.profileMap.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if (value == null) {
		    	value = "";  
		    }
		    if (key.endsWith(":") || key.equals(XMSO) || key.equals(XMS) || key.equals(XMX) || key.equals(XSCMX) || key.equals(GCTHREADS)) {
		    	content.append(key);
		    	content.append(value);
		    	content.append(NEW_LINE);
		    } else if (!value.isEmpty()) {
		    	content.append(key);
		    	content.append(EQUALS_SYBMOL);
		    	content.append(value);
		    	content.append(NEW_LINE);
		    }
		}
    	return content.toString();
	}

	@Override
	public String getJvmProfileDir() {
		return this.jvmProfileDir;
	}

	@Override
	public void printProfile() {
	    logger.info("JVM profile " + this.profileName + " parsed content:\n" + getProfileString());
	}

	@Override
	public void build() throws CicsJvmserverResourceException {
		try {
			if (this.profileUnixFile == null || !this.profileUnixFile.getFileName().equals(getProfileFileName())) {
				if (getJvmProfileDir() == null) {
					throw new CicsJvmserverResourceException("Unable to build JVM profile - JVM profile directory not supplied");
				}
				this.profileUnixFile = this.zosFileHandler.newUNIXFile(getJvmProfileDir() + SLASH_SYBMOL + getProfileFileName(), zosImage);
				this.profileUnixFile.setDataType(UNIXFileDataType.TEXT);
			}
			if (!this.profileUnixFile.exists()) {
				this.profileUnixFile.setShouldCleanup(false);
				this.profileUnixFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
			}
			this.profileUnixFile.store(getProfileString());
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Problem creating JVM profile on zOS UNIX file system", e);
		}
	}

	@Override
	public void delete() throws CicsJvmserverResourceException {
		try {
			this.profileUnixFile.delete();
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Problem deleteing the JVM profile from zOS UNIX file system", e);
		}
	}

	@Override
	public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException {
		try {
			this.profileUnixFile.saveToResultsArchive(rasPath);;
		} catch (ZosUNIXFileException e) {
			throw new CicsJvmserverResourceException("Problem saving the JVM profile from zOS UNIX file system", e);
		}
	}

	private Object getProfileFileName() {
		return this.profileName + ".jvmprofile";
	}

	@Override
	public void addProfileIncludeFile(IZosUNIXFile profileInclude) {
		if (this.profileMap.containsKey(OPTION_PC_INCLUDE)) {
			this.profileMap.put(OPTION_PC_INCLUDE, this.profileMap.get(OPTION_PC_INCLUDE) + COMMA_SYBMOL + profileInclude.getUnixPath());
		} else {
			this.profileMap.put(OPTION_PC_INCLUDE, profileInclude.getUnixPath());
		}
		this.profileIncludes.add(profileInclude);
	}

	@Override
	public IZosUNIXFile addProfileIncludeFile(String name, Map<String, String> content) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeProfileIncludeFile(IZosUNIXFile profileInclude) throws CicsJvmserverResourceException {
		if (this.profileMap.containsKey(OPTION_PC_INCLUDE)) {
			String value = this.profileMap.get(OPTION_PC_INCLUDE);
			String[] includes = value.split(COMMA_SYBMOL);
			StringBuilder newValue = new StringBuilder();
			for (String include : includes) {
				if (!include.equals(profileInclude.getUnixPath())) {
					if (newValue.length() == 0) {
						newValue.append(include);
					} else {
						newValue.append(newValue);
						newValue.append(COMMA_SYBMOL);
						newValue.append(include);
					}
				}
			}
			if (newValue.length() == 0) {
				this.profileMap.remove(OPTION_PC_INCLUDE);
			} else {
				this.profileMap.put(OPTION_PC_INCLUDE, newValue.toString());
			}
		}
		deleteProfileIncudeFile(profileInclude.getUnixPath());
	}

	private void deleteProfileIncudeFile(String unixPath) throws CicsJvmserverResourceException {
		for (IZosUNIXFile includeFile : this.profileIncludes) {
			if (includeFile.getUnixPath().equals(unixPath)) {
				try {
					includeFile.delete();
				} catch (ZosUNIXFileException e) {
					throw new CicsJvmserverResourceException("Problem deleting profile include file " + includeFile.getUnixPath(), e);			
				}
				this.profileIncludes.remove(includeFile);
				break;
			}
		}
	}

	@Override
	public void removeAllProfileIncludes() throws CicsJvmserverResourceException {
		if (this.profileMap.containsKey(OPTION_PC_INCLUDE)) {
			String value = this.profileMap.get(OPTION_PC_INCLUDE);
			String[] includes = value.split(COMMA_SYBMOL);
			for (String includeFile : includes) {
				deleteProfileIncudeFile(includeFile);
			}
			this.profileMap.remove(OPTION_PC_INCLUDE);
		}
	}

	@Override
	public void addJCCTraceProperties() throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> getJCCTraceProperties() throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveJCCTraceFiles() throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveJCCTraceFiles(String rasPath) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addRemoteDebug(int debugPort, boolean suspend) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLibertyServer(IZosLibertyServer zosLibertyServer) {
		// TODO Auto-generated method stub

	}

	@Override
	public IZosLibertyServer getLibertyServer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWlpInstallDir(String wlpInstallDir) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWlpUserDir(String wlpUserDir) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWlpOutputDir(String wlpOutputDir) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setZosConnectInstallDir() throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setZosConnectInstallDir(String zOSConnectInstallDir) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWlpServerName(String serverName) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWlpAutoconfigure(boolean autoconfigure) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWlpServerHost(String hostname) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWlpServerHttpPort(int httpPort) throws CicsJvmserverResourceException {
		this.profileMap.put("-Dcom.ibm.cics.jvmserver.wlp.server.http.port", Integer.toString(httpPort));
	}

	@Override
	public void setWlpServerHttpsPort(int httpsPort) throws CicsJvmserverResourceException {
		this.profileMap.put("-Dcom.ibm.cics.jvmserver.wlp.server.https.port", Integer.toString(httpsPort));
	}

	@Override
	public void setWlpServerWabEnabled(boolean wabEnabled) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getWlpInstallDir() throws CicsJvmserverResourceException {
		return this.profileMap.get("WLP_INSTALL_DIR");
	}

	@Override
	public String getWlpUserDir() {
		return this.profileMap.get("WLP_USER_DIR");
	}

	@Override
	public String getWlpOutputDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpAutoconfigure() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpServerHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpServerHttpPort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWlpServerHttpsPort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getWlpServerWabEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addLibertyIncludeXml(IZosUNIXFile profileInclude) {
		// TODO Auto-generated method stub

	}

	@Override
	public IZosUNIXFile addLibertyIncludeXml(String name, String content) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IZosUNIXFile addLibertyIncludeXml(String name, Document content) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLibertyIncludeXml(IZosUNIXFile name) throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllLibertyIncludeXmls() throws CicsJvmserverResourceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "[JVM profile] " + this.profileName;
	}
}
