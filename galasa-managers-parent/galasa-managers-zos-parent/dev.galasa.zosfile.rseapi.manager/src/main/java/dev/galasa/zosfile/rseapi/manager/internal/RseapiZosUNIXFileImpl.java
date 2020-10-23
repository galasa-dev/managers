/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
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
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;

public class RseapiZosUNIXFileImpl implements IZosUNIXFile {
    
    IRseapiRestApiProcessor rseapiApiProcessor;

    // zOS Image
    private IZosImage image;

    private static final String SLASH = "/";
    private static final String COMMA = ",";
    private static final String RESTFILES_FILE_PATH = SLASH + "rseapi" + SLASH + "api" + SLASH + "v1" + SLASH + "unixfiles";
    private static final String RESTFILES_FILE_PATH_RAW_CONTENT = SLASH + "rawContent";
    private static final String PATH_EQUALS = "?path=";
    
    private boolean retainToTestEnd = false;

    private String unixPath;
    private String fileName;
    private String directoryPath;
    private String type;

    private boolean pathCreated;
    private String createdPath;
    private boolean deleted;

    private String mode;

    private UNIXFileDataType dataType;
    
	private static final String PROP_PERMISSIONS_SYMBOLIC = "permissionsSymbolic";
	private static final String PROP_SIZE = "size";
	private static final String PROP_LAST_MODIFIED = "lastModified";
	private static final String PROP_TYPE = "type";
	private static final String PROP_NAME = "name";
	private static final String PROP_FILE_OWNER = "fileOwner";
	private static final String PROP_ENCODING = "encoding";
	private static final String PROP_GROUP = "group";
    private static final String PROP_CHILDREN = "children";
    private static final String PROP_PERMISSIONS = "permissions";
    private static final String PROP_CONTENT = "content";
    
    private static final String HEADER_CONVERT = "convert";

	private static final String TYPE_FILE = "FILE";
    private static final String TYPE_DIRECTORY = "DIRECTORY";

    private static final String LOG_UNIX_PATH = "UNIX path ";
    private static final String LOG_DOES_NOT_EXIST = " does not exist";
    private static final String LOG_ARCHIVED_TO = " archived to ";
    private static final String LOG_INVALID_REQUETS = "Invalid request, ";
    private static final String LOG_UNABLE_TO_LIST_UNIX_PATH = "Unable to list UNIX path ";

    private static final Log logger = LogFactory.getLog(RseapiZosUNIXFileImpl.class);

	public RseapiZosUNIXFileImpl(IZosImage image, String unixPath) throws ZosUNIXFileException {
        if (!unixPath.startsWith(SLASH)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + "must be absolute not be relative");
        }
        
        this.image = image;
        this.unixPath = unixPath;
        splitUnixPath();
        
        try {
            this.rseapiApiProcessor = RseapiZosFileManagerImpl.rseapiManager.newRseapiRestApiProcessor(this.image, RseapiZosFileManagerImpl.zosManager.getZosFilePropertyFileRestrictToImage(image.getImageID()));
            this.mode = RseapiZosFileManagerImpl.zosManager.getZosFilePropertyUnixFilePermissions(this.image.getImageID());
        } catch (ZosFileManagerException | RseapiManagerException e) {
            throw new ZosUNIXFileException(e);
        }
    }


    @Override
    public IZosUNIXFile create() throws ZosUNIXFileException {
        if (exists()) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(this.unixPath) + " already exists" + logOnImage());
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
        
        String urlPath;
        RseapiRequestType requestType;
        Object requestBody;   
        Map<String, String> headers = new HashMap<>();
        if (getDataType().equals(UNIXFileDataType.TEXT)) {
        	urlPath = RESTFILES_FILE_PATH + this.unixPath;
        	requestType = RseapiRequestType.PUT_JSON;
	        requestBody = new JsonObject();
	        headers.put(HEADER_CONVERT, "true");
	        ((JsonObject) requestBody).addProperty(PROP_CONTENT, content);
        } else {        	
        	urlPath = RESTFILES_FILE_PATH + this.unixPath + RESTFILES_FILE_PATH_RAW_CONTENT;
        	requestType = RseapiRequestType.PUT;
        	requestBody = content;
        	headers.put(HEADER_CONVERT, "false");
        }        
    
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(requestType, urlPath, headers, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
        
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            // Error case
        	String displayMessage = buildErrorString("writing to " + quoted(this.unixPath), response); 
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
        return listDirectory(this.unixPath, true);
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
    
    
    protected String getAttributesAsString(String path) throws ZosUNIXFileException {
        if (path.endsWith(SLASH)) {
            path = path.substring(0, path.length()-1);
        }
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        StringBuilder attributes = new StringBuilder();
        
        Map<String, String> headers = new HashMap<>();
        String urlPath = RESTFILES_FILE_PATH + PATH_EQUALS + path;
        
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
            
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosUNIXFileException("Unable to list " + LOG_UNIX_PATH + quoted(path) + logOnImage(), e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            attributes.append("Name=");
            attributes.append(path);
            attributes.append(COMMA);
            attributes.append("Type=");
            String typeValue = emptyStringWhenNull(responseBody, PROP_TYPE);
            attributes.append(typeValue);
            attributes.append(COMMA);
            if (typeValue.equals(TYPE_DIRECTORY)) {
                attributes.append("IsEmpty=");
                JsonArray children = responseBody.getAsJsonArray(PROP_CHILDREN);
                attributes.append(children == null? "true" : "false");
                attributes.append(COMMA);            	
            }
            attributes.append("Mode=");
            attributes.append(emptyStringWhenNull(responseBody, PROP_PERMISSIONS_SYMBOLIC));
            attributes.append(COMMA);
            attributes.append("Size=");
            attributes.append(emptyStringWhenNull(responseBody, PROP_SIZE));
            attributes.append(COMMA);
            attributes.append("User=");
            attributes.append(emptyStringWhenNull(responseBody, PROP_FILE_OWNER));
            attributes.append(COMMA);
            attributes.append("Group=");
            attributes.append(emptyStringWhenNull(responseBody, PROP_GROUP));
            attributes.append(COMMA);
            attributes.append("Modified=");
            attributes.append(emptyStringWhenNull(responseBody, PROP_LAST_MODIFIED));
            attributes.append(COMMA);
            attributes.append("Encoding=");
            attributes.append(emptyStringWhenNull(responseBody, PROP_ENCODING));            
        } else {
            // Error case
            String displayMessage = buildErrorString("creating path " + quoted(path), response);
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
        logger.trace("Attibutes of "+ LOG_UNIX_PATH + quoted(path) + " retrieved from  image " + this.image.getImageID());
        return attributes.toString();
    }

    protected boolean createPath(String path, String type) throws ZosUNIXFileException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty(PROP_TYPE, type);
        requestBody.addProperty(PROP_PERMISSIONS, this.mode);
        
        String urlPath = RESTFILES_FILE_PATH + SLASH + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.POST_JSON, urlPath, null, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_CREATED) {            
            // Error case
            String displayMessage = buildErrorString("creating path " + quoted(path), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
        return true;
    }


    protected void delete(String path, boolean recursive) throws ZosUNIXFileException {
        if (!exists(path)) {
            throw new ZosUNIXFileException(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
        }
        Map<String, String> headers = new HashMap<>();
        String attributes = getAttributesAsString(path);
        boolean isDirectory = attributes.contains("Type=" + TYPE_DIRECTORY);
        if (recursive) {
            if (!isDirectory) {
                throw new ZosUNIXFileException(LOG_INVALID_REQUETS + LOG_UNIX_PATH + quoted(path) + " is not a directory");
            }
        } else {
        	if (isDirectory && attributes.contains("IsEmpty=false")) {
                throw new ZosUNIXFileException(LOG_INVALID_REQUETS + LOG_UNIX_PATH + quoted(path) + " is a directory and is not empty. Use the directoryDeleteNonEmpty() method");
            }
        }
        String urlPath = RESTFILES_FILE_PATH + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.DELETE, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {            
            // Error case
            String displayMessage = buildErrorString("creating path " + quoted(path), response); 
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
        String urlPath = RESTFILES_FILE_PATH + SLASH + PATH_EQUALS + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, null, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            logger.trace(LOG_UNIX_PATH + quoted(path) + " exists" + logOnImage());
            return true;
        } else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            logger.trace(LOG_UNIX_PATH + quoted(path) + LOG_DOES_NOT_EXIST + logOnImage());
            return false;
        } else {
        	String displayMessage = buildErrorString("listing path " + quoted(path), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    }


    protected String retrieve(String path) throws ZosUNIXFileException {
    	String urlPath = RESTFILES_FILE_PATH + path;
        if (getDataType().equals(UNIXFileDataType.BINARY)) {
        	urlPath = urlPath + RESTFILES_FILE_PATH_RAW_CONTENT;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_CONVERT, String.valueOf(getDataType().equals(UNIXFileDataType.TEXT)));
        
        
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_OK) {            
            // Error case
            String displayMessage = buildErrorString("retrieve content " + quoted(path), response); 
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }

        String content = "";
        JsonObject responseBody;
        try {
        	responseBody = response.getJsonContent();            
            logger.trace(responseBody);
            if (responseBody.get(PROP_CONTENT) != null) {
            	content = responseBody.get(PROP_CONTENT).getAsString();
            }
        } catch (RseapiException e) {
        	throw new ZosUNIXFileException("Unable to retrieve content of " + quoted(path) + logOnImage(), e);
        }
    
        logger.trace("Content of " + LOG_UNIX_PATH + quoted(path) + " retrieved from  image " + this.image.getImageID());
        return content;
    }


    protected void saveToResultsArchive(String path) throws ZosUNIXFileException {
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


    protected boolean isDirectory(String path) throws ZosUNIXFileException {
        if (path.equals(this.unixPath) && !exists(path)) {
            return this.type.equals(TYPE_DIRECTORY);
        }
        return getAttributesAsString(path).contains("Type=" + TYPE_DIRECTORY);
    }


    protected Map<String, String> listDirectory(String path, boolean recursive) throws ZosUNIXFileException {
        if (!isDirectory(path)) {
            throw new ZosUNIXFileException(LOG_INVALID_REQUETS + quoted(path) + " is not a directory");
        }

        if (path.endsWith(SLASH)) {
            path = path.substring(0, path.length()-1);
        }
        Map<String, String> headers = new HashMap<>();
        String urlPath = RESTFILES_FILE_PATH + PATH_EQUALS + path;
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, urlPath, headers, null, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(e);
        }
            
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosUNIXFileException(LOG_UNABLE_TO_LIST_UNIX_PATH + quoted(path) + logOnImage(), e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            return getPaths(path, responseBody, recursive);
        } else {
            // Error case
        	String displayMessage = buildErrorString("listing path " + quoted(path), response);
            logger.error(displayMessage);
            throw new ZosUNIXFileException(displayMessage);
        }
    }

    protected Map<String, String> getPaths(String root, JsonObject responseBody, boolean recursive) throws ZosUNIXFileException {
    	if (!root.endsWith(SLASH)) {
    		root = root + SLASH;
    	}
		SortedMap<String, String> paths = new TreeMap<>();
		JsonArray children = responseBody.getAsJsonArray(PROP_CHILDREN);
		if (children != null) {
			for (JsonElement childElement : children) {
				JsonObject child = childElement.getAsJsonObject();
				String nameValue = root + child.get(PROP_NAME).getAsString();
				String typeValue = child.get(PROP_TYPE).getAsString();
				paths.put(nameValue, typeValue);
				if (recursive && typeValue.equals(TYPE_DIRECTORY)) {
					paths.putAll(listDirectory(nameValue, recursive));
				}
			}
		}
        return paths;
    }
    
    protected String storeArtifact(Object content, boolean directory, @NotEmpty String ... artifactPathElements) throws ZosUNIXFileException {
        Path artifactPath;
        try {
            artifactPath = RseapiZosFileManagerImpl.getUnixPathArtifactRoot().resolve(RseapiZosFileManagerImpl.currentTestMethodArchiveFolderName);
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
            this.type = TYPE_DIRECTORY;
        } else {
            int index = this.unixPath.lastIndexOf('/');
            this.fileName = this.unixPath.substring(++index);
            this.directoryPath = this.unixPath.substring(0,index-1);
            this.type = TYPE_FILE;
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
        return "'" + name + "'";
    }

    protected String logOnImage() {
        return " on image " + this.image.getImageID();
    }

    protected String buildErrorString(String action, IRseapiResponse response) {
    	String message = "";
    	try {
    		Object content = response.getContent();
			if (content != null) {
				logger.trace(content);
				if (content instanceof JsonObject) {
					message = "\nstatus: " + ((JsonObject) content).get("status").getAsString() + "\n" + "message: " + ((JsonObject) content).get("message").getAsString(); 
				} else if (content instanceof String) {
					message = " response body:\n" + content;
				}
			}
		} catch (RseapiException e) {
			// NOP
		}
        return "Error " + action + ", HTTP Status Code " + response.getStatusCode() + " : " + response.getStatusLine() + message;
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

    public boolean retainToTestEnd() {
        return this.retainToTestEnd;
    }

    public boolean deleted() {
        return this.deleted;
    }
}
