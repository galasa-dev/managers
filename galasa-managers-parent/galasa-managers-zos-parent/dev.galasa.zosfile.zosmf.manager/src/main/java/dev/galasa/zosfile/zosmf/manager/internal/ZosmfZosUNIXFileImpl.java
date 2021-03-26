/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

public class ZosmfZosUNIXFileImpl implements IZosUNIXFile {
    
    IZosmfRestApiProcessor zosmfApiProcessor;

    private Path testMethodArchiveFolder;

	private ZosmfZosFileHandlerImpl zosFileHandler;
	public ZosmfZosFileHandlerImpl getZosFileHandler() {
		return zosFileHandler;
	}

    // zOS Image
    private IZosImage image;

    private static final String SLASH = "/";
    private static final String COMMA = ",";
    private static final String RESTFILES_FILE_SYSTEM_PATH = SLASH+ "zosmf" + SLASH + "restfiles" + SLASH + "fs";
    private static final String PATH_EQUALS = "?path=";

    private String unixPath;
    private String fileName;
    private String directoryPath;
    private UNIXFileType fileType;
    private String createMode;
    private UNIXFileDataType dataType;
	private Set<PosixFilePermission> filePermissions;
	private int fileSize = -1;
	private String lastModified;
	private String user;
	private String group;

    private boolean pathCreated;
    private String createdPath;
    private boolean deleted;

    private boolean shouldArchive = false;

    private boolean shouldCleanup = true;

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
	private static final String PROP_RERQUEST = "request";
	private static final String PROP_RECURSIVE = "recursive";

    private static final String LOG_UNIX_PATH = "UNIX path ";
    private static final String LOG_LISTING = "listing";
    private static final String LOG_READING_FROM = "reading from";
    private static final String LOG_WRITING_TO = "writing to";
    private static final String LOG_DOES_NOT_EXIST = " does not exist";
    private static final String LOG_ARCHIVED_TO = " archived to ";
    private static final String LOG_INVALID_REQUETS = "Invalid request, ";
    private static final String LOG_UNABLE_TO_LIST_UNIX_PATH = "Unable to list UNIX path ";

    private static final Log logger = LogFactory.getLog(ZosmfZosUNIXFileImpl.class);

    public ZosmfZosUNIXFileImpl(ZosmfZosFileHandlerImpl zosFileHandler, IZosImage image, String unixPath) throws ZosUNIXFileException {
        if (!unixPath.startsWith(SLASH)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + "must be absolute not be relative");
        }
        
        this.image = image;
        this.unixPath = unixPath;
        this.zosFileHandler = zosFileHandler;
        this.testMethodArchiveFolder = this.zosFileHandler.getZosFileManager().getUnixPathCurrentTestMethodArchiveFolder();
        splitUnixPath();
        
        try {
            this.zosmfApiProcessor = this.zosFileHandler.getZosmfManager().newZosmfRestApiProcessor(this.image, this.zosFileHandler.getZosManager().getZosFilePropertyFileRestrictToImage(image.getImageID()));
            this.maxItems = this.zosFileHandler.getZosManager().getZosFilePropertyDirectoryListMaxItems(image.getImageID());
            this.createMode = this.zosFileHandler.getZosManager().getZosFilePropertyUnixFilePermissions(this.image.getImageID());
        } catch (ZosFileManagerException | ZosmfManagerException e) {
            throw new ZosUNIXFileException(e);
        }
    }


    @Override
    public IZosUNIXFile create() throws ZosUNIXFileException {
        return create(PosixFilePermissions.fromString(this.createMode));
    }
    
    @Override
	public IZosUNIXFile create(Set<PosixFilePermission> accessPermissions) throws ZosUNIXFileException {
        if (exists()) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + " already exists" + logOnImage());
        }
        String[] directoryPathParts = this.directoryPath.substring(1).split(SLASH);
        StringBuilder path = new StringBuilder();
        path.append(SLASH);
        for (String part : directoryPathParts) {
            path.append(part);
            if (!exists(path.toString())) {
                createPath(path.toString(), UNIXFileType.DIRECTORY, accessPermissions);
                if (this.createdPath == null) {
                    this.createdPath = path.toString() + SLASH;
                }
            }
            path.append(SLASH);
        }
        if (this.fileName != null) {
            createPath(this.unixPath, this.fileType, accessPermissions);
        }
        
        if (exists()) {
            logger.info(LOG_UNIX_PATH + quoted(this.unixPath) + " created" + logOnImage());
            this.pathCreated = true;
        } else {
            logger.info(LOG_UNIX_PATH + quoted(this.unixPath) + " not created" + logOnImage());
        }
        return this;
	}


	@Override
    public boolean delete() throws ZosUNIXFileException {
        delete(this.unixPath, false);
        return this.deleted;
    }
    
    @Override
    public boolean directoryDeleteNonEmpty() throws ZosUNIXFileException {
        delete(this.unixPath, true);
        return this.deleted;
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
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT && response.getStatusCode() != HttpStatus.SC_CREATED) {
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR            
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosUNIXFileException("Unable to write to " + LOG_UNIX_PATH + quoted(this.unixPath) + logOnImage(), e);
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
    public void saveToResultsArchive(String rasPath) throws ZosUNIXFileException {
        saveToResultsArchive(this.unixPath, rasPath);
    }
    
    @Override
    public boolean isDirectory() throws ZosUNIXFileException {
        return isDirectory(this.unixPath);
    }

    @Override
    public SortedMap<String, IZosUNIXFile> directoryList() throws ZosUNIXFileException {
        return listDirectory(this.unixPath, false);
    }

    @Override
    public SortedMap<String, IZosUNIXFile> directoryListRecursive() throws ZosUNIXFileException {
        return listDirectory(this.unixPath, true);
    }


    @Override
	public void setDataType(UNIXFileDataType dataType) {
	    this.dataType = dataType;
	}


	@Override
	public void setAccessPermissions(Set<PosixFilePermission> accessPermissions, boolean recursive) throws ZosUNIXFileException {
		setAccessPermissions(this.unixPath, accessPermissions, recursive);
	}


	@Override
    public UNIXFileType getFileType() {
    	return this.fileType;
    }


    @Override
    public UNIXFileDataType getDataType() {
        if (this.dataType == null) {
            return UNIXFileDataType.TEXT;
        }
        return this.dataType;
    }


	@Override
	public Set<PosixFilePermission> getFilePermissions() throws ZosUNIXFileException {
		if (this.filePermissions == null) {
			retrieveAttibutes();
		}
		return this.filePermissions;
	}


	@Override
	public int getSize() throws ZosUNIXFileException {
		if (this.fileSize == -1) {
			retrieveAttibutes();
		}
		return this.fileSize;
	}


	@Override
	public String getLastModified() throws ZosUNIXFileException {
		if (this.lastModified == null) {
			retrieveAttibutes();
		}
		return this.lastModified;
	}


	@Override
	public String getUser() throws ZosUNIXFileException {
		if (this.user == null) {
			retrieveAttibutes();
		}
		return this.user;
	}


	@Override
	public String getGroup() throws ZosUNIXFileException {
		if (this.group == null) {
			retrieveAttibutes();
		}
		return this.group;
	}


	@Override
	public void retrieveAttibutes() throws ZosUNIXFileException {
		getAttributes(this.unixPath);
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
        return attributesToString(getAttributes(this.unixPath));
    }

    @Override
	public void setShouldArchive(boolean shouldArchive) {
		this.shouldArchive = shouldArchive;
	}

	@Override
	public boolean shouldArchive() {
		return this.shouldArchive;
	}

    @Override
	public void setShouldCleanup(boolean shouldCleanup) {
		this.shouldCleanup = shouldCleanup;
	}

	@Override
	public boolean shouldCleanup() {
		return this.shouldCleanup;
	}
    
    protected JsonObject getAttributes(String path) throws ZosUNIXFileException {
        if (path.endsWith(SLASH)) {
            path = path.substring(0, path.length()-1);
        }
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        
        Map<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_LSTAT.toString(), "true");
        String urlPath = RESTFILES_FILE_SYSTEM_PATH + PATH_EQUALS + path;
        
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(e);
        }
            
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException("Unable to list " + LOG_UNIX_PATH + quoted(path) + logOnImage(), e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
        	JsonObject attributes = items.get(0).getAsJsonObject();
        	if (path.equals(this.unixPath)) {
				JsonElement element = attributes .get(PROP_MODE);
	            if (element != null) {
	            	this.filePermissions = PosixFilePermissions.fromString(element.getAsString().substring(1));
	            	this.fileType = determineType(element.getAsString());
	            }
	        	element = attributes.get(PROP_SIZE);
	            if (element != null) {
	            	this.fileSize = element.getAsInt();
	            }
	            element = attributes.get(PROP_USER);
	            if (element != null) {
	            	this.user = element.getAsString();
	            }
	            element = attributes.get(PROP_GROUP);
	            if (element != null) {
	            	this.group = element.getAsString();
	            }
	            element = attributes.get(PROP_MTIME);
	            if (element != null) {
	            	this.lastModified = element.getAsString();
	            }
        	}
            return attributes;
            
        } else {
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString(LOG_LISTING, responseBody, path);
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    }
    
    protected String attributesToString(JsonObject item) {
        StringBuilder attributes = new StringBuilder();
        attributes.append("Name=");
        attributes.append(emptyStringWhenNull(item, PROP_NAME));
        attributes.append(COMMA);
        attributes.append("Type=");
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
        return attributes.toString();
    }

    protected UNIXFileType determineType(String mode) {
        String typeChar = mode.substring(0, 1);
        switch(typeChar) {
            case "-": return UNIXFileType.FILE;
            case "c": return UNIXFileType.CHARACTER;
            case "d": return UNIXFileType.DIRECTORY;
            case "e": return UNIXFileType.EXTLINK;
            case "l": return UNIXFileType.SYMBLINK;
            case "p": return UNIXFileType.FIFO;
            case "s": return UNIXFileType.SOCKET;
            default: return UNIXFileType.UNKNOWN;
        }
    }


    protected boolean createPath(String path, UNIXFileType type, Set<PosixFilePermission> accessPermissions) throws ZosUNIXFileException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty(PROP_TYPE, type.toString());
        requestBody.addProperty(PROP_MODE, IZosUNIXFile.posixFilePermissionsToSymbolicNotation(accessPermissions));
        
        String urlPath = RESTFILES_FILE_SYSTEM_PATH + path;
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.POST_JSON, urlPath, null, requestBody,
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_CREATED) {            
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR            
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosUNIXFileException("Unable to create " + LOG_UNIX_PATH + quoted(this.unixPath) + logOnImage(), e);
            }
            
            logger.trace(responseBody);
            
            String displayMessage = buildErrorString("creating", responseBody, this.unixPath); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
        setAccessPermissions(path, accessPermissions, false);
        
        return true;
    }


    protected void setAccessPermissions(String unixPath, Set<PosixFilePermission> accessPermissions, boolean recursive) throws ZosUNIXFileException {
		JsonObject requestBody = new JsonObject();
	    requestBody.addProperty(PROP_RERQUEST, "chmod");
	    requestBody.addProperty(PROP_MODE, IZosUNIXFile.posixFilePermissionsToOctal(accessPermissions));
	    requestBody.addProperty(PROP_RECURSIVE, String.valueOf(recursive));
	    String urlPath = RESTFILES_FILE_SYSTEM_PATH + unixPath;
	    IZosmfResponse response;
	    try {
	        response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_JSON, urlPath, new HashMap<>(), requestBody, 
	                new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT, HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
	    } catch (ZosmfException e) {
	        throw new ZosUNIXFileException(e);
	    }
	    
	    if (response.getStatusCode() != HttpStatus.SC_OK) {
	        // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR            
	        JsonObject responseBody;
	        try {
	            responseBody = response.getJsonContent();
	        } catch (ZosmfException e) {
	            throw new ZosUNIXFileException("Unable to change file access permissions of " + LOG_UNIX_PATH + quoted(this.unixPath) + logOnImage(), e);
	        }
	        logger.trace(responseBody);
	        String displayMessage = buildErrorString("Unable to change file access permissions of " + LOG_UNIX_PATH + quoted(this.unixPath) + logOnImage(), responseBody, this.unixPath); 
	        logger.error(displayMessage);
	        throw new ZosUNIXFileException(displayMessage);
	    }
	
	    logger.trace("File access permissions of " + LOG_UNIX_PATH + quoted(this.directoryPath) + " updated" + logOnImage());
	}


	protected void delete(String path, boolean recursive) throws ZosUNIXFileException {
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        Map<String, String> headers = new HashMap<>();
        if (recursive) {
            if (!isDirectory(path)) {
                throw new ZosUNIXFileException(LOG_INVALID_REQUETS + LOG_UNIX_PATH + quoted(path) + " is not a directory");
            }
            headers.put(ZosmfCustomHeaders.X_IBM_OPTION.toString(), PROP_RECURSIVE);
        }
        String urlPath = RESTFILES_FILE_SYSTEM_PATH + path;
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, urlPath, headers, null, 
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosUNIXFileException("Unable to delete " + LOG_UNIX_PATH + quoted(path) + logOnImage(), e);
            }
            
            logger.trace(responseBody);
            String displayMessage = buildErrorString("deleting", responseBody, path); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
        
        if (exists(path)) {
            logger.info(LOG_UNIX_PATH + quoted(path) + " not deleted" + logOnImage());
            this.deleted = false;
        } else {
            logger.info(LOG_UNIX_PATH + quoted(path) + " deleted" + logOnImage());
            this.deleted = true;
        }
    }


    protected boolean exists(String path) throws ZosUNIXFileException {
        if (path.endsWith(SLASH)) {
            path = path.substring(0, path.length()-1);
        }
        Map<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_LSTAT.toString(), "true");
        String urlPath = RESTFILES_FILE_SYSTEM_PATH + PATH_EQUALS + path;
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(e);
        }
            
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(LOG_UNABLE_TO_LIST_UNIX_PATH + quoted(path) + logOnImage(), e);
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


    protected String retrieve(String path) throws ZosUNIXFileException {
        Map<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_DATA_TYPE.toString(), getDataType().toString());
        String urlPath = RESTFILES_FILE_SYSTEM_PATH + path;
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(e);
        }        
    
        String content;
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            try {
                content = response.getTextContent();
            } catch (ZosmfException e) {
                throw new ZosUNIXFileException("Unable to retrieve content of " + quoted(path) + logOnImage(), e);
            }
        } else {
            
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosUNIXFileException("Unable to retrieve content of " + quoted(path) + logOnImage(), e);
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

    
    protected void saveToResultsArchive(String path, String rasPath) throws ZosUNIXFileException {
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        if (isDirectory(path)) {
            Map<String, IZosUNIXFile> paths = listDirectory(path, true);
            for (Map.Entry<String, IZosUNIXFile> entry : paths.entrySet()) {
                String entryPath = entry.getKey();
                UNIXFileType entryFileType = entry.getValue().getFileType();
                if (!entryFileType.equals(UNIXFileType.FILE)) {
                    String archiveLocation = storeArtifact(rasPath, retrieve(entryPath), false, StringUtils.stripStart(entryPath, SLASH).split(SLASH));
                    logger.info(quoted(entryPath) + LOG_ARCHIVED_TO + archiveLocation);
                } else if (entryFileType.equals(UNIXFileType.DIRECTORY)) {
                    String archiveLocation = storeArtifact(rasPath, null, true, StringUtils.stripStart(entryPath, SLASH).split(SLASH));
                    logger.info(quoted(entryPath) + LOG_ARCHIVED_TO + archiveLocation);
                }
            }
        } else {
            String archiveLocation = storeArtifact(rasPath, retrieve(path), false, this.unixPath);
            logger.info(quoted(this.unixPath) + LOG_ARCHIVED_TO + archiveLocation);
        }
    }


    protected boolean isDirectory(String path) throws ZosUNIXFileException {
        if (path.equals(this.unixPath) && !exists(path)) {
            return this.fileType.equals(UNIXFileType.DIRECTORY);
        }
        getAttributes(path);
        return getFileType() == UNIXFileType.DIRECTORY;
    }


    protected SortedMap<String, IZosUNIXFile> listDirectory(String path, boolean recursive) throws ZosUNIXFileException {
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
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(e);
        }
            
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosUNIXFileException(LOG_UNABLE_TO_LIST_UNIX_PATH + quoted(path) + logOnImage(), e);
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


    protected SortedMap<String, IZosUNIXFile> getPaths(String root, JsonObject responseBody, boolean recursive) throws ZosUNIXFileException {
        if (!root.endsWith(SLASH)) {
            root = root + SLASH;
        }
        int returnedRowsValue = responseBody.get(PROP_RETURNED_ROWS).getAsInt();
        int totalRowsValue = responseBody.get(PROP_TOTAL_ROWS).getAsInt();
        if (totalRowsValue > returnedRowsValue) {
            throw new ZosUNIXFileException("The number of files and directories (" + totalRowsValue  + ") in UNIX path " + quoted(root) + " is greater than the maximum allowed rows (" + Integer.toString(this.maxItems) + ")");
        }
        SortedMap<String, IZosUNIXFile> paths = new TreeMap<>();
        if (returnedRowsValue > 0) {
            JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);            
            for (int i = 0; i < returnedRowsValue; i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                String path = root + item.get(PROP_NAME).getAsString();
                UNIXFileType pathType = determineType(item.get(PROP_MODE).getAsString().substring(1));
                if (!(path.endsWith("/.") || path.endsWith("/.."))) {
                	ZosmfZosUNIXFileImpl unixFile = new ZosmfZosUNIXFileImpl(this.zosFileHandler, this.image, path);
                	unixFile.setFileType(pathType);
                	unixFile.setFilePermissions(item.get(PROP_MODE).getAsString().substring(1));
                	unixFile.setFileSize(item.get(PROP_SIZE).getAsInt());
                	unixFile.setLastModified(item.get(PROP_MTIME).getAsString());
                	unixFile.setUser(item.get(PROP_USER).getAsString());
                	unixFile.setGroup(item.get(PROP_GROUP).getAsString());
                    paths.put(path, unixFile);
                    if (pathType.equals(UNIXFileType.DIRECTORY) && recursive) {
                        paths.putAll(listDirectory(path, recursive));
                    }
                }
            }
        }
        return paths;
    }
    
    protected void setFileType(UNIXFileType type) {
        this.fileType = type;
    }
    
    protected void setFilePermissions(String filePermissions) {
        this.filePermissions = PosixFilePermissions.fromString(filePermissions);
    }
    
    protected void setFileSize(int fileSize) {
    	this.fileSize = fileSize;
    }
    
    protected void setLastModified(String lastModified) {
    	this.lastModified = lastModified;
    }
    
    protected void setUser(String user) {
    	this.user = user;
    }
    
    protected void setGroup(String group) {
    	this.group = group;
    }
    
    //TODO: Investigate artifactPathElements
    protected String storeArtifact(String rasPath, Object content, boolean directory, String ... artifactPathElements) throws ZosUNIXFileException {
        Path artifactPath;
        try {
        	artifactPath = this.zosFileHandler.getArtifactsRoot().resolve(rasPath);
            if (directory) {
                Files.createDirectories(artifactPath);
            } else {
                Files.createFile(artifactPath, ResultArchiveStoreContentType.TEXT);
                if (content instanceof String) {
                    Files.write(artifactPath, ((String) content).getBytes()); 
                } else if (content instanceof byte[]) {
                    Files.write(artifactPath, (byte[]) content);
                } else {
                    throw new ZosUNIXFileException("Unable to store artifact. Invalid content object type: " + content.getClass().getName());
                }
            }
        } catch (IOException e) {
            throw new ZosUNIXFileException("Unable to store artifact", e);
        }
        return artifactPath.toString();
    }
    
    protected void splitUnixPath() {
        if (this.unixPath.endsWith(SLASH)) {
            this.fileName = null;
            this.directoryPath = this.unixPath.substring(0,this.unixPath.length()-1);
            this.fileType = UNIXFileType.DIRECTORY;
        } else {
            int index = this.unixPath.lastIndexOf('/');
            this.fileName = this.unixPath.substring(++index);
            this.directoryPath = this.unixPath.substring(0,index-1);
            this.fileType = UNIXFileType.FILE;
        }
    }

    protected String emptyStringWhenNull(JsonObject jsonElement, String property) {
        JsonElement element = jsonElement.get(property);
        if (element == null) {
            return "";
        }
        return element.getAsString();
    }

    protected String quoted(String name) {
        return "\"" + name + "\"";
    }

    protected String logOnImage() {
        return " on image " + this.image.getImageID();
    }
    
    protected String buildErrorString(String action, JsonObject responseBody, String path) {
        if ("{}".equals(responseBody.toString())) {
            return "Error " + action;
        }
        int errorCategory = responseBody.get("category").getAsInt();
        int errorRc = responseBody.get("rc").getAsInt();
        int errorReason = responseBody.get("reason").getAsInt();
        String errorMessage = responseBody.get("message").getAsString();
        String errorDetails = null;
        JsonElement element = responseBody.get("details");
        if (element != null) {
            if (element.isJsonArray()) {
                JsonArray elementArray = element.getAsJsonArray();
                StringBuilder sb = new StringBuilder();
                for (JsonElement item : elementArray) {
                    sb.append("\n");
                    sb.append(item.getAsString());
                }
                errorDetails = sb.toString();
            } else {
                errorDetails = element.getAsString();
            }
        }
        StringBuilder sb = new StringBuilder(); 
        sb.append("Error "); 
        sb.append(action);
        sb.append(" UNIX path ");
        sb.append(quoted(path));
        sb.append(", category:");
        sb.append(errorCategory);
        sb.append(", rc:");
        sb.append(errorRc);
        sb.append(", reason:");
        sb.append(errorReason);
        sb.append(", message:");
        sb.append(errorMessage);
        if (errorDetails != null) {
            sb.append("\ndetails:");
            sb.append(errorDetails);
        }
        JsonElement stackElement = responseBody.get("stack");
        if (stackElement != null) {
            sb.append("\nstack:\n");
            sb.append(stackElement.getAsString());
        }
        
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
            	if (this.shouldArchive()) {
            		cleanCreatedPathStore();
            	}
                cleanCreatedDelete();
            }
        } catch (ZosUNIXFileException e) {
            logger.error(e);
        }
    }
    
    protected void cleanCreatedPathStore() {
        try {
            saveToResultsArchive(this.createdPath);
        } catch (ZosUNIXFileException e) {
            logger.error(e);
        }
    }
    
    protected void cleanCreatedDelete() {
        try {
            delete(this.createdPath, true);
        } catch (ZosUNIXFileException e) {
            logger.error(e);
        }
    }

    public boolean deleted() {
        return this.deleted;
    }
    
    protected void archiveContent() throws ZosUNIXFileException {
    	if (shouldArchive()) {
    		Path rasPath = this.testMethodArchiveFolder.resolve(this.zosFileHandler.getZosManager().buildUniquePathName(testMethodArchiveFolder, this.unixPath.substring(1)));
            saveToResultsArchive(rasPath.toString());
        }
    }
}
