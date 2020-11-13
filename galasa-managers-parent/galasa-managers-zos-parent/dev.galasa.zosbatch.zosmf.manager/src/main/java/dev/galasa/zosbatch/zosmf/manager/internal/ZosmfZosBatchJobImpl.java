/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutput;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.spi.IZosBatchJobOutputSpi;
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * Implementation of {@link IZosBatchJob} using zOS/MF
 *
 */
public class ZosmfZosBatchJobImpl implements IZosBatchJob {
    
    private IZosmfRestApiProcessor zosmfApiProcessor;
    
    private IZosImage jobImage;
    private IZosBatchJobname jobname;
    private final ZosBatchJobcard jobcard;
    private String jcl;  
    private int intdrLrecl = 80;  
    private String intdrRecfm = "F";    
    private int jobWaitTimeout;
    
    private String jobid;         
    private String owner;         
    private String type;         
    private JobStatus status;         
    private String statusString;
    private boolean jobNotFound;
    private String retcode;
    private boolean jobComplete;
    private boolean outputComplete;
    private boolean jobArchived;
    private boolean jobPurged;
    private String jobPath;
    private String jobFilesPath;
    private IZosBatchJobOutputSpi jobOutput;
    private boolean useSysaff;
    private boolean shouldArchive = true;
    
    private static final String PROP_REASON = "reason";
    private static final String PROP_RC = "rc";
    private static final String PROP_CATEGORY = "category";
    private static final String PROP_JOBID = "jobid";
    private static final String PROP_STATUS = "status";
    private static final String PROP_OWNER = "owner";
    private static final String PROP_TYPE = "type";
    private static final String PROP_RETCODE = "retcode";
    private static final String PROP_ID = "id";
    private static final String PROP_MESSAGE = "message";
    private static final String PROP_DETAILS = "details";
    private static final String PROP_STACK = "stack";
    
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    public static final String RESTJOBS_PATH = SLASH + "zosmf" + SLASH + "restjobs" + SLASH + "jobs";
    
    private static final String LOG_JOB_NOT_SUBMITTED = "Job has not been submitted by manager";
    
    private static final Log logger = LogFactory.getLog(ZosmfZosBatchJobImpl.class);

    public ZosmfZosBatchJobImpl(IZosImage jobImage, IZosBatchJobname jobname, String jcl, ZosBatchJobcard jobcard) throws ZosBatchException {
        this.jobImage = jobImage;
        this.jobname = jobname;
        if (jobcard != null) {
            this.jobcard = jobcard;
        } else {
            this.jobcard = new ZosBatchJobcard();
        }
        if (jcl != null) {
        	Path artifactPath = ZosmfZosBatchManagerImpl.getCurrentTestMethodArchiveFolder();
        	try {
				ZosmfZosBatchManagerImpl.zosManager.storeArtifact(artifactPath.resolve(ZosmfZosBatchManagerImpl.zosManager.buildUniquePathName(artifactPath, this.jobname.getName() + "_supplied_JCL")), jcl, ResultArchiveStoreContentType.TEXT);
			} catch (ZosManagerException e) {
				throw new ZosBatchException(e);
			}
            this.jcl = parseJcl(jcl);

            try {
                this.useSysaff = ZosmfZosBatchManagerImpl.zosManager.getZosBatchPropertyUseSysaff(this.jobImage.getImageID());
            } catch (ZosBatchManagerException e) {
                throw new ZosBatchException("Unable to get use SYSAFF property value", e);
            }
        }
        
        try {
            this.jobWaitTimeout = ZosmfZosBatchManagerImpl.zosManager.getZosBatchPropertyJobWaitTimeout(this.jobImage.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Unable to get job timeout property value", e);
        }
        
        try {
            this.zosmfApiProcessor = ZosmfZosBatchManagerImpl.zosmfManager.newZosmfRestApiProcessor(jobImage, ZosmfZosBatchManagerImpl.zosManager.getZosBatchPropertyBatchRestrictToImage(jobImage.getImageID()));
        } catch (ZosmfManagerException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }
    }
    
    public @NotNull IZosBatchJob submitJob() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString(), "2.0");
        headers.put(ZosmfCustomHeaders.X_IBM_INTRDR_LRECL.toString(), String.valueOf(this.intdrLrecl));
        headers.put(ZosmfCustomHeaders.X_IBM_INTRDR_RECFM.toString(), this.intdrRecfm);
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_TEXT, RESTJOBS_PATH + SLASH, headers, jclWithJobcard(), new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }

        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_CREATED) {
            this.jobid = jsonNull(responseBody, PROP_JOBID);
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.retcode = jsonNull(responseBody, PROP_RETCODE);
            setJobPathValues();
            logger.info("JOB " + this + " Submitted");
        } else {            
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString("Submit job", responseBody); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        
        return this;
    }
    
    @Override
    public IZosBatchJobname getJobname() {
        return this.jobname;
    }
    
    @Override
    public String getJobId() {
    	if (this.jobid == null) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}    		
    	}
        return (this.jobid != null ? this.jobid : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getOwner() {
    	if (this.owner == null) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}    		
    	}
        return (this.owner != null ? this.owner : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getType() {
    	if (this.type == null) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}    		
    	}
        return (this.type != null ? this.type : StringUtils.repeat(QUERY, 3));
    }
    
    @Override
    public JobStatus getStatus() {
    	if (this.status != JobStatus.OUTPUT) {
	    	try {
				updateJobStatus();
			} catch (ZosBatchException e) {
				logger.error(e);
			}
    	}
    	return (this.status != null ? this.status : JobStatus.UNKNOWN);
    }
    
    @Override
    public String getStatusString() {
    	if (this.status != JobStatus.OUTPUT) {
	    	try {
				updateJobStatus();
			} catch (ZosBatchException e) {
				logger.error(e);
			}
    	}
        return (this.statusString != null ? this.statusString : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getRetcode() {
    	if (this.retcode == null) {
        	try {
    			updateJobStatus();
    		} catch (ZosBatchException e) {
    			logger.error(e);
    		}    		
    	}
        return (this.retcode != null ? this.retcode : StringUtils.repeat(QUERY, 4));
    }

    @Override
    public int waitForJob() throws ZosBatchException {
        if (!submitted()) {
            throw new ZosBatchException(LOG_JOB_NOT_SUBMITTED);
        }
        logger.info("Waiting up to " + jobWaitTimeout + " second(s) for "+ this.jobid + " " + this.jobname.getName() + " to complete");
        
        LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(jobWaitTimeout);
        while (LocalDateTime.now().isBefore(timeoutTime)) {
            updateJobStatus();
            if (this.jobNotFound) {
                return Integer.MIN_VALUE;
            }
            try {
                if (isComplete()) {
                    String[] rc = this.retcode.split(" ");
                    if (rc.length == 2) {
                        return StringUtils.isNumeric(rc[1]) ? Integer.parseInt(rc[1]) : Integer.MIN_VALUE;
                    }
                    return Integer.MIN_VALUE;
                }
                
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("waitForJob Interrupted", e);
                Thread.currentThread().interrupt();
                throw new ZosBatchException(e);
            }
        }
        return Integer.MIN_VALUE;
    }    
    
    @Override
    public IZosBatchJobOutput retrieveOutput() throws ZosBatchException {
        if (!this.outputComplete) {
            getOutput();
        }
        
        return jobOutput();
    }

	@Override
	public String retrieveOutputAsString() throws ZosBatchException {
		StringBuilder output = new StringBuilder();
		retrieveOutput().getSpoolFiles().forEach(records -> output.append(records.getRecords()));
        return output.toString();		
	}
    
    @Override
    public void cancel() throws ZosBatchException {
        if (!isComplete()) {
            cancel(false);
        }
    }

    @Override
    public void purge() throws ZosBatchException {
        if (!isPurged()) {
            cancel(true);
        }
    }
    
    @Override
    public IZosBatchJobOutputSpoolFile getSpoolFile(@NotNull String ddname) throws ZosBatchException {
        Iterator<IZosBatchJobOutputSpoolFile> spoolFilesIterator = retrieveOutput().iterator();
        while (spoolFilesIterator.hasNext()) {
            IZosBatchJobOutputSpoolFile spoolFile = spoolFilesIterator.next();
            if (spoolFile.getDdname().equals(ddname)) {
                return spoolFile;
            }
        }
        return null;
    }

    @Override
    public void saveOutputToResultsArchive() throws ZosBatchException {
    	if (!shouldArchive()) {
    		throw new ZosBatchException("shouldArchive flag is false");
    	}
        if (jobOutput() == null) {
            retrieveOutput();
        }
        Path artifactPath = ZosmfZosBatchManagerImpl.getCurrentTestMethodArchiveFolder();
        String folderName = this.jobname.getName() + "_" + this.jobid + "_" + this.retcode.replace(" ", "-").replace(StringUtils.repeat(QUERY, 4), "UNKNOWN");
        artifactPath = artifactPath.resolve(ZosmfZosBatchManagerImpl.zosManager.buildUniquePathName(artifactPath, folderName));
        
        Iterator<IZosBatchJobOutputSpoolFile> iterator = jobOutput().iterator();
        while (iterator.hasNext()) {
            IZosBatchJobOutputSpoolFile spoolFile = iterator.next();
            StringBuilder name = new StringBuilder();
            name.append(spoolFile.getJobname());
            name.append("_");
            name.append(spoolFile.getJobid());
            if (!spoolFile.getStepname().isEmpty()){
                name.append("_");
                name.append(spoolFile.getStepname());
            }
            if (!spoolFile.getProcstep().isEmpty()){
                name.append("_");
                name.append(spoolFile.getProcstep());
            }
            name.append("_");
            name.append(spoolFile.getDdname());
            logger.info("        " + name);
            String fileName = ZosmfZosBatchManagerImpl.zosManager.buildUniquePathName(artifactPath, name.toString());
            try {
				ZosmfZosBatchManagerImpl.zosManager.storeArtifact(artifactPath.resolve(fileName), spoolFile.getRecords(), ResultArchiveStoreContentType.TEXT);
			} catch (ZosManagerException e) {
				throw new ZosBatchException(e);
			}
        }
        if (isComplete()) {
        	this.jobArchived = true;
        }
    }

    @Override
	public void setShouldArchive(boolean shouldArchive) {
		this.shouldArchive = shouldArchive;
	}

	@Override
	public boolean shouldArchive() {
		return this.shouldArchive;
	}

	protected void getOutput() throws ZosBatchException {
    
        if (!submitted()) {
            throw new ZosBatchException(LOG_JOB_NOT_SUBMITTED);
        }
        updateJobStatus();
        if (this.jobNotFound) {
            return;
        }
        
        // First, get a list of spool files
        this.jobOutput = ZosmfZosBatchManagerImpl.zosManager.newZosBatchJobOutput(this.jobname.getName(), this.jobid);
        this.jobFilesPath = RESTJOBS_PATH + SLASH + this.jobname.getName() + SLASH + this.jobid + "/files";
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, this.jobFilesPath, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
    
        Object responseBodyObject;
        try {
            responseBodyObject = response.getContent();
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        logger.trace(responseBodyObject);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            // Get the spool files
            JsonArray jsonArray;
            try {
                jsonArray = response.getJsonArrayContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject responseBody = jsonElement.getAsJsonObject();
                String id = jsonNull(responseBody, PROP_ID);
                addOutputFileContent(responseBody, this.jobFilesPath + "/" + id + "/records");
            }
        } else {            
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString("Retrieve job output", (JsonObject) responseBodyObject); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        
        // Get the JCLIN
        addOutputFileContent(null, this.jobFilesPath + "/JCL/records");
        
        if (this.jobComplete) {
            this.outputComplete = true;
        }
    }

    protected void setJobid(String jobid) {
        this.jobid = jobid;
    }
    
    protected void setOwner(String owner) {
        this.owner = owner;
    }
    
    protected void setType(String type) {
        this.type = type;
    }
    
    protected void setStatusString(String statusString) {
        this.statusString = statusString;
        setStatus(statusString);
    }
    
    protected void setStatus(String statusString) {
    	//zOSMF Status: INPUT | ACTIVE | OUTPUT
        this.status = JobStatus.valueOfLabel(statusString);
    }

    protected void cancel(boolean purge) throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString(), "2.0");
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            if (purge) {
                response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, this.jobPath, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
            } else {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("request", "cancel");
                requestBody.addProperty("version", "2.0");
                response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_JSON, this.jobPath, headers, requestBody, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
            }
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }

        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfManagerException e) {
            throw new ZosBatchException(e);
        }
        
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            this.statusString = null;
            if (purge) {
                this.jobPurged = true; 
            } else {
                this.jobComplete = true;
            }
        } else {            
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString(purge ? "Purge job" : "Cancel job", responseBody); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
    }
        
    @Override
    public String toString() {
        if (!isPurged()) {
            try {
                updateJobStatus();
            } catch (ZosBatchException e) {
                logger.error(e);
            }
        }
        return jobStatus();        
    }

    public boolean submitted() {
    	return !getJobId().contains("?");
    }
    
    public boolean isComplete() {
        return this.jobComplete;
    }

    public boolean isArchived() {
        return this.jobArchived;
    }

    public boolean isPurged() {
        return this.jobPurged;
    }

    public IZosBatchJobOutput jobOutput() {
        return this.jobOutput;
    }

    private String jobStatus() {
        return "JOBID=" + this.jobid + 
              " JOBNAME=" + this.jobname.getName() + 
              " OWNER=" + this.owner + 
              " TYPE=" + this.type +
              " STATUS=" + this.status.toString() + 
              " RETCODE=" + this.retcode;
    }

    protected void setJobPathValues() {
        this.jobPath = RESTJOBS_PATH + SLASH + this.jobname.getName() + SLASH + this.jobid;
        this.jobFilesPath = this.jobPath + "/files";
    }

    protected void updateJobStatus() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, RESTJOBS_PATH + SLASH + this.jobname.getName() + "/" + this.jobid, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
        
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
            
        logger.trace(responseBody);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            this.jobNotFound = false;
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.statusString = jsonNull(responseBody, PROP_STATUS);
            if (this.statusString != null && "OUTPUT".equals(this.statusString)) {
                this.jobComplete = true;
            }
            this.status = JobStatus.valueOfLabel(statusString);
            String retcodeProperty = jsonNull(responseBody, PROP_RETCODE);
            if (retcodeProperty != null) {
                this.retcode = retcodeProperty;
            } else {
                this.retcode = StringUtils.repeat(QUERY, 4);
            }
            logger.debug(jobStatus());
        } else {
            if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST &&
                    jsonZero(responseBody, PROP_RC) == 4 &&
                    jsonZero(responseBody, PROP_REASON) == 10) {
                logger.warn("JOBID=" + this.jobid + " JOBNAME=" + this.jobname.getName() + " NOT FOUND");
                this.jobNotFound = true;
                this.statusString = null;
            } else {
                // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
                String displayMessage = buildErrorString("Update job status", responseBody); 
                logger.error(displayMessage);
                throw new ZosBatchException(displayMessage);
            }
        }            
    }

    protected void addOutputFileContent(JsonObject responseBody, String path) throws ZosBatchException {
    
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString(), "");
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, path, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosBatchException(e);
        }
    
        String fileOutput;
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            try {
                fileOutput = response.getTextContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
        } else {            
            // Error case
            JsonObject errorResponseBody;
            try {
                errorResponseBody = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
            if (this.jobComplete && spoolFileNotFound(errorResponseBody)) {
                return; 
            } else {
                String displayMessage = buildErrorString("Retrieve job output", errorResponseBody);
                logger.error(displayMessage);
                throw new ZosBatchException(displayMessage);
            }
        }
        if (responseBody != null) {
            String stepname = jsonNull(responseBody, "stepname");
            String procstep = jsonNull(responseBody, "procstep");
            String ddname = responseBody.get("ddname").getAsString();
            this.jobOutput.addSpoolFile(stepname, procstep, ddname, fileOutput);
        } else {
            this.jobOutput.addJcl(fileOutput);
        }
    }

    protected boolean spoolFileNotFound(JsonObject errorResponseBody) {
        return (jsonZero(errorResponseBody, PROP_CATEGORY) == 6 &&
                jsonZero(errorResponseBody, PROP_RC) == 4 &&
                jsonZero(errorResponseBody, PROP_REASON) == 12);
    }

    protected String parseJcl(String jcl) throws ZosBatchException {
        boolean truncateJCLRecords;
        try {
            truncateJCLRecords = ZosmfZosBatchManagerImpl.zosManager.getZosBatchPropertyTruncateJCLRecords(this.jobImage.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Unable to get trucate JCL records property value", e);
        }

        List<String> jclRecords = new LinkedList<>(Arrays.asList(jcl.split("\n")));
        
        if (truncateJCLRecords) {
            return truncateJcl(jclRecords);
        } else {
            parseJclForVaryingRecordLength(jclRecords);
            return jcl;
        }
    }

    private String truncateJcl(List<String> jclRecords) {
        List<String> recordsOut = new LinkedList<>();
        List<Integer> truncatedRecords = new ArrayList<>();
        for (int i=0; i < jclRecords.size(); i++) {
            String record = jclRecords.get(i);
            int recordLength = record.length();
            if (recordLength > 80) {
                // Truncate records to 80 bytes
                truncatedRecords.add(i+1);
                recordsOut.add(record.substring(0, 80));
            } else {
                recordsOut.add(record);
            }
        }
        if (!truncatedRecords.isEmpty()) {
            logger.warn("The following record(s) have been truncated to 80 characters: " + truncatedRecords.toString());
        }
        return String.join("\n", recordsOut);
    }

    private void parseJclForVaryingRecordLength(List<String> jclRecords) {
        int minRecordLength = 0;
        for (int i=0; i < jclRecords.size(); i++) {
            int recordLength = jclRecords.get(i).length();
            if (recordLength > 80) {
                if (recordLength > this.intdrLrecl) {
                    this.intdrLrecl = recordLength;
                    if (minRecordLength == 0) {
                        minRecordLength = recordLength;
                    }
                }
                if (recordLength <= minRecordLength) {
                    minRecordLength = recordLength;
                }
            }
        }
        if (minRecordLength != 0 && minRecordLength != this.intdrLrecl) {
            this.intdrRecfm = "V";
        }
    }

    protected String jclWithJobcard() throws ZosBatchManagerException {
        StringBuilder jclWithJobcard = new StringBuilder();
        jclWithJobcard.append(this.jobcard.getJobcard(this.jobname.getName(), this.jobImage));
        
        if (this.useSysaff) {
            jclWithJobcard.append("/*JOBPARM SYSAFF=");
            jclWithJobcard.append(this.jobImage.getImageID());
            jclWithJobcard.append("\n");
        }
        
        logger.info("JOBCARD:\n" + jclWithJobcard.toString());
        jclWithJobcard.append(jcl);
        if (!jclWithJobcard.toString().endsWith("\n")) {
            jclWithJobcard.append("\n");
        }
        return jclWithJobcard.toString();
    }

    protected String jsonNull(JsonObject responseBody, String memberName) {
        if (responseBody.get(memberName) != null && !responseBody.get(memberName).isJsonNull()) {
            return responseBody.get(memberName).getAsString();
        }
        return null;
    }

    protected int jsonZero(JsonObject responseBody, String memberName) {
        if (responseBody.get(memberName) != null && !responseBody.get(memberName).isJsonNull()) {
            return responseBody.get(memberName).getAsInt();
        }
        return 0;
    }

    protected static String buildErrorString(String action, JsonObject responseBody) {
        if ("{}".equals(responseBody.toString())) {
            return "Error " + action;
        }
        int errorCategory = responseBody.get(PROP_CATEGORY).getAsInt();
        int errorRc = responseBody.get(PROP_RC).getAsInt();
        int errorReason = responseBody.get(PROP_REASON).getAsInt();
        String errorMessage = responseBody.get(PROP_MESSAGE).getAsString();
        String errorDetails = null;
        JsonElement element = responseBody.get(PROP_DETAILS);
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
        JsonElement stackElement = responseBody.get(PROP_STACK);
        if (stackElement != null) {
            sb.append("\nstack:\n");
            sb.append(stackElement.getAsString());
        }
        
        return sb.toString();
    }

    protected void archiveJobOutput() throws ZosBatchException {
    	if (shouldArchive() && (!isArchived() || !this.jobComplete)) {
            saveOutputToResultsArchive();
        }
    }
}
