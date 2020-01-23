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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.zosmf.manager.internal.properties.RestrictZosmfToImage;
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

public class ZosDatasetImpl implements IZosDataset {
	
	private IZosmfRestApiProcessor zosmfApiProcessor;

	// zOS Image
	private IZosImage image;

	private static final String SLASH = "/";
	private static final String COMMA = ",";
	private static final String RESTFILES_DATASET_PATH = SLASH + "zosmf" + SLASH + "restfiles" + SLASH + "ds";
	
	// data set and member names
	private String dsname;
	private boolean datasetCreated = false;
	private boolean retainToTestEnd = false;
	private boolean temporary = false;
	
	private Collection<String> datasetMembers;
	private String memberStart = null;

	// parameters
	private String volser = null;
	private String unit = null;
	private String dsorg = null;
	private String alcunit = null;
	private int primary = -1;
	private int secondary = -1;
	private int dirblk = -1;
	private int avgblk = -1;
	private String recfm = null;
	private int blksize = -1;
	private int lrecl = -1;
	private String storeclass = null;
	private String mgntclass = null;
	private String dataclass = null;
	private String dstype = null;

	private DatasetDataType dataType;

	private static final String PROP_VOLSER = "volser";     
	private static final String PROP_UNIT = "unit";       
	private static final String PROP_DSORG = "dsorg";      
	private static final String PROP_ALCUNIT = "alcunit";    
	private static final String PROP_PRIMARY = "primary";    
	private static final String PROP_SECONDARY = "secondary";  
	private static final String PROP_DIRBLK = "dirblk";     
	private static final String PROP_AVGBLK = "avgblk";     
	private static final String PROP_RECFM = "recfm";      
	private static final String PROP_BLKSIZE = "blksize";    
	private static final String PROP_LRECL = "lrecl";      
	private static final String PROP_STORECLASS = "storeclass";
	private static final String PROP_MGNTCLASS = "mgntclass";
	private static final String PROP_DATACLASS = "dataclass";
	private static final String PROP_DSTYPE = "dstype";
	private static final String PROP_DSNAME = "dsname";
	private static final String PROP_RETURNED_ROWS = "returnedRows";
	private static final String PROP_MORE_ROWS = "moreRows";
	private static final String PROP_ITEMS = "items";
	private static final String PROP_MEMBER = "member";

	private static final String PROP_BLKSZ = "blksz";
	private static final String PROP_EXTX = "extx";
	private static final String PROP_USED = "used";
	private static final String PROP_CDATE = "cdate";
	private static final String PROP_RDATE = "rdate";
	private static final String PROP_EDATE = "edate";
	private static final String PROP_VOL = "vol";
	
	private static final String LOG_DATA_SET = "Data set ";
	private static final String LOG_MEMBER = "Member ";
	private static final String LOG_LISTING = "listing";
	private static final String LOG_READING_FROM = "reading from";
	private static final String LOG_WRITING_TO = "writing to";
	private static final String LOG_DOES_NOT_EXIST = " does not exist";
	private static final String LOG_ARCHIVED_TO = " archived to ";

	private static final Log logger = LogFactory.getLog(ZosDatasetImpl.class);

	public ZosDatasetImpl(IZosImage image, String dsname) throws ZosDatasetException {
		
		this.image = image;
		splitDSN(dsname);
		
		try {
			this.zosmfApiProcessor = ZosFileManagerImpl.zosmfManager.newZosmfRestApiProcessor(this.image, RestrictZosmfToImage.get(image.getImageID()));
		} catch (ZosFileManagerException | ZosmfManagerException e) {
			throw new ZosDatasetException(e);
		}
	}

	@Override
	public IZosDataset create() throws ZosDatasetException {
		if (exists()) {
			throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " aleady exists" + logOnImage());
		}
		
		JsonObject requestBody = new JsonObject();
		requestBody = addPropertyWhenSet(requestBody, PROP_VOLSER, this.volser);
		requestBody = addPropertyWhenSet(requestBody, PROP_UNIT, this.unit);
		requestBody = addPropertyWhenSet(requestBody, PROP_DSORG, this.dsorg);
		requestBody = addPropertyWhenSet(requestBody, PROP_ALCUNIT, this.alcunit);
		requestBody = addPropertyWhenSet(requestBody, PROP_PRIMARY, this.primary);
		requestBody = addPropertyWhenSet(requestBody, PROP_SECONDARY, this.secondary);
		requestBody = addPropertyWhenSet(requestBody, PROP_DIRBLK, this.dirblk);
		requestBody = addPropertyWhenSet(requestBody, PROP_AVGBLK, this.avgblk);
		requestBody = addPropertyWhenSet(requestBody, PROP_RECFM, this.recfm);
		requestBody = addPropertyWhenSet(requestBody, PROP_BLKSIZE, this.blksize);
		requestBody = addPropertyWhenSet(requestBody, PROP_LRECL, this.lrecl);
		requestBody = addPropertyWhenSet(requestBody, PROP_STORECLASS, this.storeclass);
		requestBody = addPropertyWhenSet(requestBody, PROP_MGNTCLASS, this.mgntclass);
		requestBody = addPropertyWhenSet(requestBody, PROP_DATACLASS, this.dataclass);
		requestBody = addPropertyWhenSet(requestBody, PROP_DSTYPE, this.dstype);
		
		String urlPath = RESTFILES_DATASET_PATH + SLASH  + this.dsname;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.POST_JSON, urlPath, null, requestBody,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}

		if (response.getStatusCode() != HttpStatus.SC_CREATED) {			
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR			
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosDatasetException("Unable to create data set " + quoted(this.dsname) + logOnImage());
			}
			
			logger.trace(responseBody);
			
			String displayMessage = buildErrorString("creating", responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
		
		if (exists()) {
			String retained = "";
			if (this.retainToTestEnd) {
				retained = " and will be retained until the end of this test run";
			}
			logger.info(LOG_DATA_SET + quoted(this.dsname) + " created" + logOnImage() + retained);
			this.datasetCreated = true;
		} else {
			logger.info(LOG_DATA_SET + quoted(this.dsname) + " not created" + logOnImage());
		}
		return this;
	}

	@Override
	public IZosDataset createRetain() throws ZosDatasetException {
		this.retainToTestEnd = true;
		return create();
	}
	
	public IZosDataset createTemporary() throws ZosDatasetException {
		this.temporary = true;
		return create();
	}

	@Override
	public void delete() throws ZosDatasetException {
		if (!created()) {
			throw new ZosDatasetException(quoted(this.dsname) + " not created by this test run " + logOnImage());
		}
		if (!exists()) {
			throw new ZosDatasetException(quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		
		String urlPath = RESTFILES_DATASET_PATH + SLASH + this.dsname;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, urlPath, null, null, 
					new ArrayList<>(Arrays.asList(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}
		
		if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosDatasetException("Unable to delete data set " + quoted(this.dsname) + logOnImage());
			}
			
			logger.trace(responseBody);
			String displayMessage = buildErrorString("deleting", responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
		
		if (exists()) {
			logger.info(LOG_DATA_SET + quoted(this.dsname) + " not deleted" + logOnImage());
		} else {
			logger.info(LOG_DATA_SET + quoted(this.dsname) + " deleted" + logOnImage());
		}
	}

	@Override
	public boolean exists() throws ZosDatasetException {
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_MAX_ITEMS.toString(), "1");
		String urlPath = RESTFILES_DATASET_PATH + "?dslevel=" + this.dsname;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}
		
		JsonObject responseBody;
		try {
			responseBody = response.getJsonContent();
		} catch (ZosmfException e) {
			throw new ZosDatasetException("Unable to list data set " + quoted(this.dsname) + logOnImage());
		}
		
		logger.trace(responseBody);
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			int returnedRowsValue = responseBody.get(PROP_RETURNED_ROWS).getAsInt();
			if (returnedRowsValue == 1) {
				JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
				JsonObject item = items.get(0).getAsJsonObject();
				String dsnameValue = item.get(PROP_DSNAME).getAsString();
				if (this.dsname.equals(dsnameValue)) {
					logger.trace(LOG_DATA_SET + quoted(this.dsname) + " exists" + logOnImage());
					return true;
				}
			}
		} else {			
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			String displayMessage = buildErrorString(LOG_LISTING, responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
	
		logger.trace(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
		return false;
	}

	@Override
	public void store(@NotNull String content) throws ZosDatasetException {
		if (isPDS()) {
			throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " is a partitioned data data set.  Use memberStore(String memberName, String content) method instead");
		}
		store(content, null);
	}

	@Override
	public String retrieve() throws ZosDatasetException {
		if (isPDS()) {
			throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + " is a partitioned data set.  Use retrieve(String memberName) method instead");
		}
		return retrieve(null);
	}
	
	@Override
	public void saveToResultsArchive() throws ZosDatasetException {
		try {
			if (exists()) {
				if (isPDS()) {
					Collection<String> memberList = memberList();
					Iterator<String> memberListIterator = memberList.iterator();
					while (memberListIterator.hasNext()) {
						String memberName = memberListIterator.next();
						String archiveLocation = storeArtifact(retrieve(memberName), this.dsname, memberName);
						logger.info(quoted(joinDSN(memberName)) + LOG_ARCHIVED_TO + archiveLocation);
					}
				} else {
					String archiveLocation = storeArtifact(retrieve(), this.dsname);
					logger.info(quoted(this.dsname) + LOG_ARCHIVED_TO + archiveLocation);
				}
			}
		} catch (ZosFileManagerException e) {
			logger.error("Unable to save data set to archive", e);
		}
	}

	@Override
	public boolean isPDS() throws ZosDatasetException {
		return getAttibutesAsString().contains("PDS=true");
	}

	@Override
	public void memberCreate(@NotNull String memberName) throws ZosDatasetException {
		store("", memberName);
	}

	@Override
	public void memberDelete(@NotNull String memberName) throws ZosDatasetException {
		if (!exists()) {
			logger.info(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
			return;
		}

		if (!memberExists(memberName)) {
			logger.info(LOG_MEMBER + memberName + LOG_DOES_NOT_EXIST + "in " + LOG_DATA_SET + quoted(this.dsname) + logOnImage());
			return;
		}
		
		String urlPath = RESTFILES_DATASET_PATH + SLASH + joinDSN(memberName);
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, urlPath, null, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}
		
		if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosDatasetException("Unable to delete member " + memberName + " from data set " + quoted(this.dsname) + logOnImage());
			}
			
			logger.trace(responseBody);
			String displayMessage = buildErrorString("deleting", responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
		
		if (memberExists(memberName)) {
			logger.info(LOG_MEMBER + memberName + " not deleted from data set " + quoted(this.dsname) + logOnImage());
		} else {
			logger.info(LOG_MEMBER + memberName + " deleted from data set " + quoted(this.dsname) + logOnImage());
		}
	}

	@Override
	public boolean memberExists(@NotNull String memberName) throws ZosDatasetException {
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_MAX_ITEMS.toString(), "1");
		String urlPath = RESTFILES_DATASET_PATH + SLASH + this.dsname + "/member?pattern=" + memberName;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, null, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}
		
		JsonObject responseBody;
		try {
			responseBody = response.getJsonContent();
		} catch (ZosmfException e) {
			throw new ZosDatasetException("Unable to list members of data set " + quoted(this.dsname) + logOnImage());
		}
		
		logger.trace(responseBody);
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			int returnedRowsValue = responseBody.get(PROP_RETURNED_ROWS).getAsInt();
			if (returnedRowsValue == 1) {
				JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
				JsonObject item = items.get(0).getAsJsonObject();
				String memberValue = item.get(PROP_MEMBER).getAsString();
				if (memberName.equals(memberValue)) {
					logger.trace("Data set member " + quoted(joinDSN(memberName)) + " exists" + logOnImage());
					return true;
				}
			}
		} else {			
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			String displayMessage = buildErrorString(LOG_LISTING, responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
	
		logger.trace("Data set member " + quoted(joinDSN(memberName)) + LOG_DOES_NOT_EXIST + logOnImage());
		return false;
	}

	@Override
	public void memberStore(@NotNull String memberName, @NotNull String content) throws ZosDatasetException {
		store(content, memberName);
	}

	@Override
	public String memberRetrieve(@NotNull String memberName) throws ZosDatasetException {
		return retrieve(memberName);
	}

	@Override
	public Collection<String> memberList() throws ZosDatasetException {
		this.datasetMembers = new ArrayList<>();
		this.memberStart = null;
		boolean moreRows = true;

		while (moreRows) {
			String urlPath = RESTFILES_DATASET_PATH + SLASH + this.dsname + SLASH + PROP_MEMBER + (this.memberStart != null ? "?start=" + this.memberStart : "");
			IZosmfResponse response;
			try {
				response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, null, null,
						new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
			} catch (ZosmfException e) {
				throw new ZosDatasetException(e);
			}
			
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosDatasetException("Unable to retrieve member list of data set " + quoted(this.dsname) + logOnImage());
			}
			
			logger.trace(responseBody);
			if (response.getStatusCode() == HttpStatus.SC_OK) {
				moreRows = getMembers(responseBody);
			} else {			
				// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
				String displayMessage = buildErrorString(LOG_LISTING, responseBody); 
				logger.error(displayMessage);
				throw new ZosDatasetException(displayMessage);
			}
		}
		logger.trace("Content of data set " + quoted(this.dsname) + "  retrieved from  image " + this.image.getImageID());

		return this.datasetMembers;
	}

	@Override
	public void memberSaveToTestArchive(@NotNull String memberName) throws ZosDatasetException {
		try {
			String archiveLocation = storeArtifact(memberRetrieve(memberName), this.dsname, memberName);
			logger.info(quoted(joinDSN(memberName)) + LOG_ARCHIVED_TO + archiveLocation);
		} catch (ZosFileManagerException e) {
			logger.error("Unable to save data set member to archive", e);
		}
	}

	@Override
	public void setDataType(DatasetDataType dataType) {
		logger.info("Data type set to " + dataType.toString());
		this.dataType = dataType;
	}

	@Override
	public void setUnit(String unit) {
		this.unit = unit;		
	}

	@Override
	public void setVolumes(String volumes) {
		this.volser = volumes;
	}

	@Override
	public void setDatasetOrganization(DatasetOrganization organization) {
		this.dsorg = organization.toString();
	}

	@Override
	public void setSpace(SpaceUnit spaceUnit, int primaryExtents, int secondaryExtents) {
		// Singular value rather than plural
		this.alcunit = spaceUnit.toString().replaceFirst("S$","");
		this.primary = primaryExtents;
		this.secondary = secondaryExtents;
	}

	@Override
	public void setDirectoryBlocks(int directoryBlocks) {
		this.dirblk = directoryBlocks;
	}

	@Override
	public void setRecordFormat(RecordFormat recordFormat) {
		this.recfm = recordFormat.toString();
	}

	@Override
	public void setBlockSize(int blockSize) {
		this.blksize = blockSize;
	}

	@Override
	public void setRecordlength(int recordlength) {
		this.lrecl = recordlength;
	}

	@Override
	public void setManagementClass(String managementClass) {
		this.mgntclass = managementClass;
	}

	@Override
	public void setStorageClass(String storageClass) {
		this.storeclass = storageClass;
	}

	@Override
	public void setDataClass(String dataClass) {
		this.dataclass = dataClass;
	}

	@Override
	public void setDatasetType(DSType dsType) {
		this.dstype = dsType.toString();
	}

	@Override
	public String getName() {
		return this.dsname;
	}

	@Override
	public DatasetDataType getDataType() {
		if (this.dataType == null) {
			return DatasetDataType.TEXT;
		}
		return this.dataType;
	}

	@Override
	public String getUnit() {
		return this.unit;		
	}

	@Override
	public String getVolumes() {
		return this.volser;
	}

	@Override
	public String getDatasetOrganization() {
		return this.dsorg;
	}

	@Override
	public String getSpaceUnit() {
		return this.alcunit;
	}

	@Override
	public int getPrimaryExtents() {
		return this.primary;
	}

	@Override
	public int getSecondaryExtents() {
		return this.secondary;
	}

	@Override
	public int getDirectoryBlocks() {
		return this.dirblk;
	}

	@Override
	public String getRecordFormat() {
		return this.recfm;
	}

	@Override
	public int getBlockSize() {
		return this.blksize;
	}

	@Override
	public int getRecordlength() {
		return this.lrecl;
	}

	@Override
	public String getManagementClass() {
		return this.mgntclass;
	}

	@Override
	public String getStorageClass() {
		return this.storeclass;
	}

	@Override
	public String getDataClass() {
		return this.dataclass;
	}

	@Override
	public DSType getDatasetType() {
		if (this.dstype == null) {
			return null;
		}
		return DSType.valueOf(this.dstype);
	}

	@Override
	public String getAttibutesAsString() throws ZosDatasetException {
		if (!exists()) {
			throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		StringBuilder attributes = new StringBuilder();
		
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_ATTRIBUTES.toString(), "base");
		headers.put(ZosmfCustomHeaders.X_IBM_MAX_ITEMS.toString(), "1");

		String urlPath = RESTFILES_DATASET_PATH + "?dslevel=" + this.dsname;
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}
		
		JsonObject responseBody;
		try {
			responseBody = response.getJsonContent();
		} catch (ZosmfException e) {
			throw new ZosDatasetException("Unable to attibutes of data set " + quoted(this.dsname) + logOnImage());
		}
		
		logger.trace(responseBody);
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			int returnedRowsValue = responseBody.get(PROP_RETURNED_ROWS).getAsInt();
			if (returnedRowsValue == 1) {
				JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);
				JsonObject item = items.get(0).getAsJsonObject();
				attributes.append("Data Set Name=");
				attributes.append(emptyStringWhenNull(item, PROP_DSNAME));
				attributes.append(COMMA);
				attributes.append("Volume serial=");
				attributes.append(emptyStringWhenNull(item, PROP_VOL));
				attributes.append(COMMA);
				attributes.append("Organization=");
				attributes.append(emptyStringWhenNull(item, PROP_DSORG));
				attributes.append(COMMA);
				attributes.append("Record format=");
				attributes.append(emptyStringWhenNull(item, PROP_RECFM));
				attributes.append(COMMA);
				attributes.append("Record length=");
				attributes.append(emptyStringWhenNull(item, PROP_LRECL));
				attributes.append(COMMA);
				attributes.append("Block size=");
				attributes.append(emptyStringWhenNull(item, PROP_BLKSZ));
				attributes.append(COMMA);
				attributes.append("Data set name type=");
				attributes.append(emptyStringWhenNull(item, PROP_DSTYPE));
				attributes.append(COMMA);
				attributes.append("Allocated extents=");
				attributes.append(emptyStringWhenNull(item, PROP_EXTX));
				attributes.append(COMMA);
				attributes.append("% Utilized=");
				attributes.append(emptyStringWhenNull(item, PROP_USED));
				attributes.append(COMMA);
				if (emptyStringWhenNull(item, PROP_DSORG).startsWith("PO")) {
					attributes.append("PDS=true");
					attributes.append(COMMA);
					attributes.append("Number of members=");
					attributes.append(memberList().size());
					attributes.append(COMMA);
				} else {
					attributes.append("PDS=false");
					attributes.append(COMMA);
				}
				attributes.append("Creation date=");
				attributes.append(emptyStringWhenNull(item, PROP_CDATE));
				attributes.append(COMMA);
				attributes.append("Referenced date=");
				attributes.append(emptyStringWhenNull(item, PROP_RDATE));
				attributes.append(COMMA);
				attributes.append("Expiration date=");
				attributes.append(emptyStringWhenNull(item, PROP_EDATE));
			} else {
				throw new ZosDatasetException("Unable to retrieve attibutes of data set" + quoted(this.dsname) + logOnImage());				
			}
		} else {			
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			String displayMessage = buildErrorString(LOG_LISTING, responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
		logger.trace("Attibutes of data set " + quoted(this.dsname) + "  retrieved from  image " + this.image.getImageID());
		
		return attributes.toString();
	}
	
	private String retrieve(String memberName) throws ZosDatasetException {
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_DATA_TYPE.toString(), getDataType().toString());
		String urlPath = RESTFILES_DATASET_PATH + SLASH + joinDSN(memberName);
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, urlPath, headers, null,
					new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}		
	
		String content;
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			try {
				content = response.getTextContent();
			} catch (ZosmfException e) {
				throw new ZosDatasetException("Unable to retrieve content of data set " + quoted(joinDSN(memberName)) + logOnImage());
			}
		} else {			
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosDatasetException("Unable to retrieve content of data set " + quoted(joinDSN(memberName)) + logOnImage());
			}
			logger.trace(responseBody);	
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			String displayMessage = buildErrorString(LOG_READING_FROM, responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
	
		logger.trace("Content of data set " + quoted(joinDSN(memberName)) + " retrieved from  image " + this.image.getImageID());
		return content;
	}

	private void store(String content, String memberName) throws ZosDatasetException {
		if (!exists()) {
			throw new ZosDatasetException(LOG_DATA_SET + quoted(this.dsname) + LOG_DOES_NOT_EXIST + logOnImage());
		}
		Map<String, String> headers = new HashMap<>();
		headers.put(ZosmfCustomHeaders.X_IBM_DATA_TYPE.toString(), getDataType().toString());
	
		String urlPath = RESTFILES_DATASET_PATH + SLASH + joinDSN(memberName);
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_TEXT, urlPath, headers, content, 
					new ArrayList<>(Arrays.asList(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		} catch (ZosmfException e) {
			throw new ZosDatasetException(e);
		}
		
		if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT && response.getStatusCode() != HttpStatus.SC_CREATED) {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR			
			JsonObject responseBody;
			try {
				responseBody = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosDatasetException("Unable to write to data set " + quoted(joinDSN(memberName)) + logOnImage());
			}
			logger.trace(responseBody);
			String displayMessage = buildErrorString(LOG_WRITING_TO, responseBody); 
			logger.error(displayMessage);
			throw new ZosDatasetException(displayMessage);
		}
	
		logger.trace(LOG_DATA_SET + quoted(joinDSN(memberName)) + " updated" + logOnImage());
	}

	private String storeArtifact(String content, String... artifactPathElements) throws ZosFileManagerException {
		Path artifactPath;
		try {
			artifactPath = ZosFileManagerImpl.getDatasetArtifactRoot().resolve(ZosFileManagerImpl.currentTestMethod);
			String lastElement = artifactPathElements[artifactPathElements.length-1];
			for (String artifactPathElement : artifactPathElements) {
				if (!lastElement.equals(artifactPathElement)) {
					artifactPath = artifactPath.resolve(artifactPathElement);
				}
			}
			String uniqueId = lastElement;
			if (Files.exists(artifactPath.resolve(lastElement))) {
				uniqueId = lastElement + "_" + new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss.SSS").format(new Date());
			}
			artifactPath = artifactPath.resolve(uniqueId);
			Files.createFile(artifactPath, ResultArchiveStoreContentType.TEXT);
			Files.write(artifactPath, content.getBytes()); 
		} catch (IOException e) {
			throw new ZosFileManagerException("Unable to store artifact", e);
		}
		return artifactPath.toString();
	}

	private String emptyStringWhenNull(JsonObject jsonElement, String property) {
		JsonElement element = jsonElement.get(property);
		if (element == null) {
			return "";
		}
		return element.getAsString();
	}

	private JsonObject addPropertyWhenSet(JsonObject requestBody, String property, Object value) throws ZosDatasetException {
		if (value != null) {
			if (value instanceof String) {
				requestBody.addProperty(property, (String) value);
			} else if (value instanceof Integer) {
				if ((int) value >= 0) {
					requestBody.addProperty(property, (Integer) value);
				}
			} else {
				throw new ZosDatasetException("Invlaid type of " + quoted(value.getClass().getName()) + " for property " + quoted(property) + logOnImage());
			}
		}
		return requestBody;
	}

	private boolean getMembers(JsonObject responseBody) {
		boolean moreRows = false;
	
		int returnedRowsValue = responseBody.get(PROP_RETURNED_ROWS).getAsInt();
		if (returnedRowsValue > 0) {
			JsonElement moreRowselement = responseBody.get(PROP_MORE_ROWS);
			if (moreRowselement != null) {
				moreRows = moreRowselement.getAsBoolean();
			} else {
				moreRows = false;
			}
			JsonArray items = responseBody.getAsJsonArray(PROP_ITEMS);			
			for (int i = 0; i < returnedRowsValue; i++) {
				JsonObject item = items.get(i).getAsJsonObject();
				if (moreRows && i == returnedRowsValue-1) {
					this.memberStart = item.get(PROP_MEMBER).getAsString();
				} else {
					this.datasetMembers.add(item.get(PROP_MEMBER).getAsString());
				}
			}
		}
		return moreRows;
	}

	private String buildErrorString(String action, JsonObject responseBody) {	
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
		String errorStack = responseBody.get("stack").getAsString();
		StringBuilder sb = new StringBuilder(); 
		sb.append("Error "); 
		sb.append(action); 
		sb.append(" data set ");
		sb.append(quoted(this.dsname));
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
		sb.append("\nstack:\n");
		sb.append(errorStack);
		
		return sb.toString();
	}

	/**
	 * Infer the data set and member names from the full DSN
	 * @param fullName 
	 */
	private void splitDSN(String fullName) {
		if (fullName.matches(".*\\(.*\\)")) {
			this.dsname = fullName.substring(0, fullName.indexOf('(')).trim();
		} else {
			this.dsname = fullName;
		}
	}

	/**
	 * Infer the full DSN name from data set and member names 
	 */
	private String joinDSN(String memberName) {
		if (memberName == null) {
			return this.dsname;
		}
		return this.dsname + "(" + memberName + ")";
	}
	
	private String quoted(String name) {
		return "\"" + name + "\"";
	}

	private String logOnImage() {
		return " on image " + this.image.getImageID();
	}
	
	public class MemberList {
		private List<String> members = new ArrayList<>();
		private String start;
		private boolean moreRows = false;

		public Collection<String> getMemberList() {
			return members;
		}

		public void setMoreRows(boolean moreRows) {
			this.moreRows  = moreRows;
		}

		public void setStart(String start) {
			this.start = start;			
		}

		public void add(String member) {
			this.members.add(member);
		}

		public String start() {
			return this.start;
		}

		public boolean hasMoreRows() {			
			return this.moreRows;
		}
	}

	@Override
	public String toString() {
		return this.dsname;
	}

	public boolean created() {
		return this.datasetCreated;
	}
	
	public boolean retainToTestEnd() {
		return this.retainToTestEnd;
	}

	public IZosmfRestApiProcessor getZosmfApiProcessor() {
		return this.zosmfApiProcessor;
	}

	public boolean isTemporary() {
		return this.temporary;
	}
}
