/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.zosmf.manager.internal.properties.DirectoryListMaxItems;
import dev.galasa.zosfile.zosmf.manager.internal.properties.RestrictZosmfToImage;
import dev.galasa.zosfile.zosmf.manager.internal.properties.UnixFilePermissions;
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

public class ZosUNIXFileImpl implements IZosUNIXFile {
	
	IZosmfRestApiProcessor zosmfApiProcessor;

	// zOS Image
	private IZosImage image;

	private static final String SLASH = "/";
	private static final String COMMA = ",";
	private static final String RESTFILES_FILE_SYSTEM_PATH = SLASH+ "zosmf" + SLASH + "restfiles" + SLASH + "fs";
	private static final String PATH_EQUALS = "?path=";
	
	private boolean retainToTestEnd = false;

	private String unixPath;
	private String fileName;
	private String directoryPath;
	private String type;
	private boolean isDirectory;

	private boolean pathCreated;
	private String createdPath;
	private boolean deleted;

	private String mode;

	private UNIXFileDataType dataType;

	private int maxItems;

	private static final String PROP_TYPE = "type";
	private static final String PROP_MODE = "mode";
	private static final String PROP_RETURNED_ROWS = "returnedRows";
	private static final String PROP_TOTAL_ROWS = "totalRows";
	private static final String PROP_ITEMS = "items";
	private static final String PROP_NAME = "name";	
	private static final String PROP_SIZE = "size";
	private static final String PROP_UID = "uid";
	private static final String PROP_USER = "user";
	private static final String PROP_GID = "gid";
	private static final String PROP_GROUP = "group";
	private static final String PROP_MTIME = "mtime";
	private static final String PROP_TARGET = "target";
	
	private static final String TYPE_FILE = "file";
	private static final String TYPE_CHARACTER = "character";
	private static final String TYPE_DIRECTORY = "directory";
	private static final String TYPE_EXTLINK = "extlink";
	private static final String TYPE_SYMBLINK = "symblink";
	private static final String TYPE_FIFO = "FIFO";
	private static final String TYPE_SOCKET = "socket";
	private static final String TYPE_UNKNOWN = "UNKNOWN";

	private static final String LOG_UNIX_PATH = "UNIX path ";
	private static final String LOG_LISTING = "listing";
	private static final String LOG_READING_FROM = "reading from";
	private static final String LOG_WRITING_TO = "writing to";
	private static final String LOG_DOES_NOT_EXIST = " does not exist";
	private static final String LOG_ARCHIVED_TO = " archived to ";
	private static final String LOG_INVALID_REQUETS = "Invalid requrest, ";
	private static final String LOG_UNABLE_TO_LIST_UNIX_PATH = "Unable to list UNIX path ";

	private static final Log logger = LogFactory.getLog(ZosUNIXFileImpl.class);

	public ZosUNIXFileImpl(IZosImage image, String unixPath) throws ZosUNIXFileException {
		if (!unixPath.startsWith(SLASH)) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + "must be absolute not be relative");
		}
		
		this.image = image;
		this.unixPath = unixPath;
		splitUnixPath();
		
		try {
			this.zosmfApiProcessor = ZosFileManagerImpl.zosmfManager.newZosmfRestApiProcessor(this.image, RestrictZosmfToImage.get(image.getImageID()));
			this.maxItems = DirectoryListMaxItems.get(image.getImageID());
			this.mode = UnixFilePermissions.get(this.image.getImageID());
		} catch (ZosFileManagerException | ZosmfManagerException e) {
			throw new ZosUNIXFileException(e);
		}
	}


	@Override
	public IZosUNIXFile create() throws ZosUNIXFileException {
		if (exists()) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + " aleady exists" + logOnImage());
		}
		String[] directoryPathParts = this.directoryPath.substring(1).split(SLASH);
		StringBuilder path = new StringBuilder();
		path.append(SLASH);
		for (String part : directoryPathParts) {
			path.append(part);
			if (!exists(path.toString())) {
				createPath(path.toString(), TYPE_DIRECTORY);
				if (this.createdPath == null) {
					this.createdPath = path.toString() + SLASH;
				}
			}
			path.append(SLASH);
		}
		if (this.fileName != null) {
			createPath(this.unixPath, this.type);
		}
		
		if (exists()) {
			String retained = "";
			if (this.retainToTestEnd) {
				retained = " and will be retained until the end of this test run";
			}
			logger.info(LOG_UNIX_PATH + quoted(this.unixPath) + " created" + logOnImage() + retained);
			this.pathCreated = true;
		} else {
			logger.info(LOG_UNIX_PATH + quoted(this.unixPath) + " not created" + logOnImage());
		}
		return this;
	}

	@Override
	public IZosUNIXFile createRetain() throws ZosUNIXFileException {
		this.retainToTestEnd = true;
		return create();
	}
	
	@Override
	public void delete() throws ZosUNIXFileException {
		delete(this.unixPath, false);
		this.deleted = true;
	}
	
	@Override
	public void directoryDeleteNonEmpty() throws ZosUNIXFileException {
		delete(this.unixPath, true);
		this.deleted = true;
	}


	@Override
	public boolean exists() throws ZosUNIXFileException {
		return exists(this.unixPath);
	}
	
	@Override
	public void store(String content) throws ZosUNIXFileException {
		if (!exists()) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		if (isDirectory()) {
			throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(this.unixPath) + " is a directory");
		}
		
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_DATA_TYPE.toString(), getDataType().toString());
	
		String urlPath = RESTFILES_FILE_SYSTEM_PATH + this.unixPath;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_TEXT, urlPath, headers, content, 
					new ArrayList<>(Arrays.asList(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(e);
		}
		
		if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT && response.getStatusCode() != HttpStatus.SC_CREATED) {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR			
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosUNIXFileException("Unable to write to " + LOG_UNIX_PATH + quoted(this.unixPath) + logOnImage());
			}
			logger.trace(responseBody);
			String displayMessage = buildErrorString(LOG_WRITING_TO, responseBody, this.unixPath); 
			logger.error(displayMessage);
			throw new ZosUNIXFileException(displayMessage);
		}
	
		logger.trace(LOG_UNIX_PATH + quoted(this.directoryPath) + " updated" + logOnImage());
		
	}

	@Override
	public String retrieve() throws ZosUNIXFileException {
		if (!exists()) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		if (isDirectory()) {
			throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(this.unixPath) + " is a directory");
		}
		return retrieve(this.unixPath);
	}

	@Override
	public void saveToResultsArchive() throws ZosUNIXFileException {
		saveToResultsArchive(this.unixPath);
	}
	
	@Override
	public boolean isDirectory() throws ZosUNIXFileException {
		return isDirectory(this.unixPath);
	}

	@Override
	public Map<String, String> directoryList() throws ZosUNIXFileException {
		return listDirectory(this.unixPath, false);
	}

	@Override
	public Map<String, String> directoryListRecursive() throws ZosUNIXFileException {
		return listDirectory(this.unixPath, false);
	}


	@Override
	public void setDataType(UNIXFileDataType dataType) {
		this.dataType = dataType;
	}


	@Override
	public UNIXFileDataType getDataType() {
		if (this.dataType == null) {
			return UNIXFileDataType.TEXT;
		}
		return this.dataType;
	}


	@Override
	public String getUnixPath() {
		return this.unixPath;
	}


	@Override
	public String getFileName() {
		return this.fileName;
	}


	@Override
	public String getDirectoryPath() {
		return this.directoryPath;
	}
	
	@Override 
	public String getAttributesAsString() throws ZosUNIXFileException {
		return getAttributesAsString(this.unixPath);
	}
	
	
	private String getAttributesAsString(String path) throws ZosUNIXFileException {
		if (path.endsWith(SLASH)) {
			path = path.substring(0, path.length()-1);
		}
		if (!exists(path)) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		StringBuilder attributes = new StringBuilder();
		
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_LSTAT.toString(), "true");
		String urlPath = RESTFILES_FILE_SYSTEM_PATH + PATH_EQUALS + path;
		
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(e);
		}
			
		JsonObject responseBody;
		try {
			responseBody = response.getJsonContent();
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException("Unable to list " + LOG_UNIX_PATH + quoted(path) + logOnImage());
		}
		
		logger.trace(responseBody);
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
			JsonObject item = items.get(0).getAsJsonObject();
			attributes.append("Name=");
			attributes.append(emptyStringWhenNull(item, PROP_NAME));
			attributes.append(COMMA);
			attributes.append("type=");
			attributes.append(determineType(emptyStringWhenNull(item, PROP_MODE)));
			attributes.append(COMMA);
			attributes.append("Mode=");
			attributes.append(emptyStringWhenNull(item, PROP_MODE));
			attributes.append(COMMA);
			attributes.append("Size=");
			attributes.append(emptyStringWhenNull(item, PROP_SIZE));
			attributes.append(COMMA);
			attributes.append("UID=");
			attributes.append(emptyStringWhenNull(item, PROP_UID));
			attributes.append(COMMA);
			attributes.append("User=");
			attributes.append(emptyStringWhenNull(item, PROP_USER));
			attributes.append(COMMA);
			attributes.append("GID=");
			attributes.append(emptyStringWhenNull(item, PROP_GID));
			attributes.append(COMMA);
			attributes.append("Group=");
			attributes.append(emptyStringWhenNull(item, PROP_GROUP));
			attributes.append(COMMA);
			attributes.append("Modified=");
			attributes.append(emptyStringWhenNull(item, PROP_MTIME));
			attributes.append(COMMA);
			attributes.append("Target=");
			attributes.append(emptyStringWhenNull(item, PROP_TARGET));
		} else {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			String displayMessage = buildErrorString(LOG_LISTING, responseBody, path);
			logger.error(displayMessage);
			throw new ZosUNIXFileException(displayMessage);
		}
		logger.trace("Attibutes of "+ LOG_UNIX_PATH + quoted(path) + " retrieved from  image " + this.image.getImageID());
		return attributes.toString();
	}

	private String determineType(String mode) {
		String typeChar = mode.substring(0, 1);
		switch(typeChar) {
			case "-": return TYPE_FILE;
			case "c": return TYPE_CHARACTER;
			case "d": return TYPE_DIRECTORY;
			case "e": return TYPE_EXTLINK;
			case "l": return TYPE_SYMBLINK;
			case "p": return TYPE_FIFO;
			case "s": return TYPE_SOCKET;
			default: return TYPE_UNKNOWN;
		}
	}


	private IZosUNIXFile createPath(String path, String type) throws ZosUNIXFileException {

		JsonObject requestBody = new JsonObject();
		requestBody.addProperty(PROP_TYPE, type);
		requestBody.addProperty(PROP_MODE, this.mode);
		
		String urlPath = RESTFILES_FILE_SYSTEM_PATH + path;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.POST_JSON, urlPath, null, requestBody,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(e);
		}

		if (response.getStatusCode() != HttpStatus.SC_CREATED) {			
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR			
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosUNIXFileException("Unable to create " + LOG_UNIX_PATH + quoted(this.unixPath) + logOnImage());
			}
			
			logger.trace(responseBody);
			
			String displayMessage = buildErrorString("creating", responseBody, this.unixPath); 
			logger.error(displayMessage);
			throw new ZosUNIXFileException(displayMessage);
		}
		
		return this;
	}


	private void delete(String path, boolean recursive) throws ZosUNIXFileException {
		if (!created()) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + " not created by this " + this.getClass().getInterfaces()[0].getSimpleName() + " object" + logOnImage());
		}
		if (!exists(path)) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		Map<String, String> headers = new HashMap<>();
		if (recursive) {
			if (!isDirectory(path)) {
				throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(path) + " is NOT a directory");
			}
			headers.put(ZosmfCustomHeaders.X_IBM_OPTION.toString(), "recursive");
		}
		String urlPath = RESTFILES_FILE_SYSTEM_PATH + path;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, urlPath, headers, null, 
					new ArrayList<>(Arrays.asList(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(e);
		}
		
		if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosUNIXFileException("Unable to delete " + LOG_UNIX_PATH + quoted(path) + logOnImage());
			}
			
			logger.trace(responseBody);
			String displayMessage = buildErrorString("deleting", responseBody, path); 
			logger.error(displayMessage);
			throw new ZosUNIXFileException(displayMessage);
		}
		
		if (exists(path)) {
			logger.info(LOG_UNIX_PATH + quoted(path) + " not deleted" + logOnImage());
		} else {
			logger.info(LOG_UNIX_PATH + quoted(path) + " deleted" + logOnImage());
		}
	}


	private boolean exists(String path) throws ZosUNIXFileException {
		if (path.endsWith(SLASH)) {
			path = path.substring(0, path.length()-1);
		}
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_LSTAT.toString(), "true");
		String urlPath = RESTFILES_FILE_SYSTEM_PATH + PATH_EQUALS + path;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(e);
		}
			
		JsonObject responseBody;
		try {
			responseBody = response.getJsonContent();
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(LOG_UNABLE_TO_LIST_UNIX_PATH + quoted(path) + logOnImage());
		}
		
		logger.trace(responseBody);
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			logger.trace(LOG_UNIX_PATH + quoted(path) + " exists" + logOnImage());
			return true;
		} else {
			if (response.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
				// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
				String displayMessage = buildErrorString(LOG_LISTING, responseBody, this.unixPath); 
				logger.error(displayMessage);
				throw new ZosUNIXFileException(displayMessage);
			}
		}
	
		logger.trace(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
		return false;
	}


	private String retrieve(String path) throws ZosUNIXFileException {
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_DATA_TYPE.toString(), getDataType().toString());
		String urlPath = RESTFILES_FILE_SYSTEM_PATH + path;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(e);
		}		
	
		String content;
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			try {
				content = response.getTextContent();
			} catch (ZosmfException e) {
				throw new ZosUNIXFileException( "Unable to retrieve content of " + quoted(path) + logOnImage());
			}
		} else {
			
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosUNIXFileException( "Unable to retrieve content of " + quoted(path) + logOnImage());
			}
			logger.trace(responseBody);	
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			String displayMessage = buildErrorString(LOG_READING_FROM, responseBody, path); 
			logger.error(displayMessage);
			throw new ZosUNIXFileException(displayMessage);
		}
	
		logger.trace("Content of " + LOG_UNIX_PATH + quoted(path) + " retrieved from  image " + this.image.getImageID());
		return content;
	}


	private void saveToResultsArchive(String path) throws ZosUNIXFileException {
		if (!exists(path)) {
			throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		if (isDirectory(path)) {
			Map<String, String> paths = listDirectory(path, true);
			for (Map.Entry<String,String> entry : paths.entrySet()) {
				String entryPath = entry.getKey();
				String entryType = entry.getValue();
				if (entryType.equals(TYPE_FILE)) {
					String archiveLocation = storeArtifact(retrieve(entryPath), false, StringUtils.stripStart(entryPath, SLASH).split(SLASH));
					logger.info(quoted(entryPath) + LOG_ARCHIVED_TO + archiveLocation);
				} else if (entryType.equals(TYPE_DIRECTORY)) {
					String archiveLocation = storeArtifact(null, true, StringUtils.stripStart(entryPath, SLASH).split(SLASH));
					logger.info(quoted(entryPath) + LOG_ARCHIVED_TO + archiveLocation);
				}
			}
		} else {
			String archiveLocation = storeArtifact(retrieve(path), false, this.unixPath);
			logger.info(quoted(this.unixPath) + LOG_ARCHIVED_TO + archiveLocation);
		}
	}


	private boolean isDirectory(String path) throws ZosUNIXFileException {
		if (path.equals(this.unixPath) && !exists(path)) {
			return this.isDirectory;
		}
		return getAttributesAsString(path).contains("type=" + TYPE_DIRECTORY);
	}


	private Map<String, String> listDirectory(String path, boolean recursive) throws ZosUNIXFileException {
		if (!isDirectory(path)) {
			throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(path) + " is not a directory");
		}

		if (path.endsWith(SLASH)) {
			path = path.substring(0, path.length()-1);
		}
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_LSTAT.toString(), "false");
		headers.put(ZosmfCustomHeaders.X_IBM_MAX_ITEMS.toString(), Integer.toString(this.maxItems));
		String urlPath = RESTFILES_FILE_SYSTEM_PATH + PATH_EQUALS + path;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(e);
		}
			
		JsonObject responseBody;
		try {
			responseBody = response.getJsonContent();
		} catch (ZosmfException e) {
			throw new ZosUNIXFileException(LOG_UNABLE_TO_LIST_UNIX_PATH + quoted(this.unixPath) + logOnImage());
		}
		
		logger.trace(responseBody);
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			return getPaths(path, responseBody, recursive);
		} else {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			String displayMessage = buildErrorString(LOG_LISTING, responseBody, path); 
			logger.error(displayMessage);
			throw new ZosUNIXFileException(displayMessage);
		}
	}


	private Map<String, String> getPaths(String root, JsonObject responseBody, boolean recursive) throws ZosUNIXFileException {
		if (!root.endsWith(SLASH)) {
			root = root + SLASH;
		}
		int returnedRowsValue = responseBody.get(PROP_RETURNED_ROWS).getAsInt();
		int totalRowsValue = responseBody.get(PROP_TOTAL_ROWS).getAsInt();
		if (totalRowsValue > returnedRowsValue) {
			throw new ZosUNIXFileException("The number of files and directories (" + totalRowsValue  + ") in UNIX path " + quoted(root) + " is greater than the maximum allowed rows (" + Integer.toString(this.maxItems) + ")");
		}
		SortedMap<String, String> paths = new TreeMap<>();
		if (returnedRowsValue > 0) {
			JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);			
			for (int i = 0; i < returnedRowsValue; i++) {
				JsonObject item = items.get(i).getAsJsonObject();
				String path = root + item.get(PROP_NAME).getAsString();
				String pathType = determineType(item.get(PROP_MODE).getAsString());
				if (!(path.endsWith("/.") || path.endsWith("/.."))) {
					paths.put(path, pathType);
					if (pathType.equals(TYPE_DIRECTORY)) {
						paths.putAll(listDirectory(path, recursive));
					}
				}
			}
		}
		return paths;
	}
	
	private String storeArtifact(String content, boolean directory, @NotEmpty String ... artifactPathElements) throws ZosUNIXFileException {
		Path artifactPath;
		try {
			artifactPath = ZosFileManagerImpl.getUnixPathArtifactRoot().resolve(ZosFileManagerImpl.currentTestMethod);
			String lastElement = artifactPathElements[artifactPathElements.length-1];
			for (String artifactPathElement : artifactPathElements) {
				if (!lastElement.equals(artifactPathElement)) {
					artifactPath = artifactPath.resolve(artifactPathElement);
				}
			}
			String uniquePathString = lastElement;
			if (!directory && Files.exists(artifactPath.resolve(lastElement))) {
				uniquePathString = lastElement + "_" + new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss.SSS").format(new Date());
			}
			artifactPath = artifactPath.resolve(StringUtils.stripStart(uniquePathString, SLASH));
			if (directory) {
				Files.createDirectories(artifactPath);
			} else {
				Files.createFile(artifactPath, ResultArchiveStoreContentType.TEXT);
				Files.write(artifactPath, content.getBytes());
			}
		} catch (IOException e) {
			throw new ZosUNIXFileException("Unable to store artifact", e);
		}
		return artifactPath.toString();
	}
	
	private void splitUnixPath() {
		if (this.unixPath.endsWith(SLASH)) {
			this.fileName = null;
			this.directoryPath = this.unixPath.substring(0,this.unixPath.length()-1);
			this.isDirectory  = true;
		} else {
			int index = this.unixPath.lastIndexOf('/');
			this.fileName = this.unixPath.substring(++index);
			this.directoryPath = this.unixPath.substring(0,index-1);
			this.isDirectory  = false;
		}
	}

	private String emptyStringWhenNull(JsonObject jsonElement, String property) {
		JsonElement element = jsonElement.get(property);
		if (element == null) {
			return "";
		}
		return element.getAsString();
	}

	private String quoted(String name) {
		return "\"" + name + "\"";
	}

	private String logOnImage() {
		return " on image " + this.image.getImageID();
	}
	
	private String buildErrorString(String action, JsonObject responseBody, String path) {	
		int errorCategory = responseBody.get("category").getAsInt();
		int errorRc = responseBody.get("rc").getAsInt();
		int errorReason = responseBody.get("reason").getAsInt();
		String errorMessage = responseBody.get("message").getAsString();
		String errorDetails = null;
		JsonElement element = responseBody.get("details");
		if (element != null) {
			if (element.isJsonArray()) {
				errorDetails = element.getAsJsonArray().toString();
			} else {
				errorDetails = element.getAsString();
			}
		}
		String errorStack = responseBody.get("stack").getAsString();
		StringBuilder sb = new StringBuilder(); 
		sb.append("Error "); 
		sb.append(action); 
		sb.append(" UNIX path ");
		sb.append(quoted(path));
		sb.append(" category:");
		sb.append(errorCategory);
		sb.append(" rc:");
		sb.append(errorRc);
		sb.append(" reason:");
		sb.append(errorReason);
		sb.append(" message:");
		sb.append(errorMessage);
		if (errorDetails != null) {
			sb.append(" details:");
			sb.append(errorDetails);
		}
		sb.append(" stack:\n");
		sb.append(errorStack);
		
		return sb.toString();
	}

	@Override
	public String toString() {
		return this.unixPath;
	}


	public boolean created() {
		return this.pathCreated;
	}
	
	public void cleanCreatedPath() {
		try {
			if (this.createdPath != null && exists(this.createdPath)) {
				cleanCreatedPathStore();
				cleanCreatedDelete();
			}
		} catch (ZosUNIXFileException e) {
			logger.error(e);
		}
	}
	
	private void cleanCreatedPathStore() {
		try {
			saveToResultsArchive(this.createdPath);
		} catch (ZosUNIXFileException e) {
			logger.error(e);
		}
	}
	
	private void cleanCreatedDelete() {
		try {
			delete(this.createdPath, true);
		} catch (ZosUNIXFileException e) {
			logger.error(e);
		}
	}


	public boolean retainToTestEnd() {
		return this.retainToTestEnd;
	}


	public boolean deleted() {
		return this.deleted;
	}
}
