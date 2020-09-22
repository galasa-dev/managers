/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.rseapi.manager.internal;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * Implementation of {@link IZosBatchJob} using zOS/MF
 *
 */
public class RseapiZosBatchJobImpl implements IZosBatchJob {
    
    private IRseapiRestApiProcessor rseapiApiProcessor;
    
    private IZosImage jobImage;
    private IZosBatchJobname jobname;
    private final ZosBatchJobcard jobcard;
    private String jcl;
    private int jobWaitTimeout;
    
    private String jobid;         
    private String owner;         
    private String type;         
    private String status;
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
    
    private static final String PROP_REASON = "reason";
    private static final String PROP_RC = "rc";
    private static final String PROP_CATEGORY = "category";
    private static final String PROP_JOBID = "jobId";
    private static final String PROP_OWNER = "owner";
    private static final String PROP_TYPE = "type";
    private static final String PROP_RETCODE = "returnCode";
    private static final String PROP_ID = "id";
    private static final String PROP_CONTENT = "content";
    
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    protected static final String RESTJOBS_PATH = SLASH + "rseapi" + SLASH + "api" + SLASH + "v1" +SLASH + "jobs";
    
    private static final String LOG_JOB_NOT_SUBMITTED = "Job has not been submitted by manager";
    
    private static final Log logger = LogFactory.getLog(RseapiZosBatchJobImpl.class);

    protected static final List<Integer> VALID_STATUS_CODES = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK,
                                                                                          HttpStatus.SC_CREATED,
                                                                                          HttpStatus.SC_NO_CONTENT,
                                                                                          HttpStatus.SC_BAD_REQUEST,
                                                                                          HttpStatus.SC_UNAUTHORIZED,
                                                                                          HttpStatus.SC_FORBIDDEN,
                                                                                          HttpStatus.SC_NOT_FOUND,
                                                                                          HttpStatus.SC_INTERNAL_SERVER_ERROR
                                                                                          ));

    public RseapiZosBatchJobImpl(IZosImage jobImage, IZosBatchJobname jobname, String jcl, ZosBatchJobcard jobcard) throws ZosBatchException {
        this.jobImage = jobImage;
        this.jobname = jobname;
        if (jobcard != null) {
            this.jobcard = jobcard;
        } else {
            this.jobcard = new ZosBatchJobcard();
        }
        if (jcl != null) {
        	Path artifactPath = RseapiZosBatchManagerImpl.getCurrentTestMethodArchiveFolder();
        	try {
				RseapiZosBatchManagerImpl.zosManager.storeArtifact(artifactPath.resolve(RseapiZosBatchManagerImpl.zosManager.buildUniquePathName(artifactPath, this.jobname.getName() + "_supplied_JCL")), jcl, ResultArchiveStoreContentType.TEXT);
			} catch (ZosManagerException e) {
				throw new ZosBatchException(e);
			}
            this.jcl = jcl;

            try {
                this.useSysaff = RseapiZosBatchManagerImpl.zosManager.getZosBatchPropertyUseSysaff(this.jobImage.getImageID());
            } catch (ZosBatchManagerException e) {
                throw new ZosBatchException("Unable to get use SYSAFF property value", e);
            }
        }
        
        try {
            this.jobWaitTimeout = RseapiZosBatchManagerImpl.zosManager.getZosBatchPropertyJobWaitTimeout(this.jobImage.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Unable to get job timeout property value", e);
        }
        
        try {
            this.rseapiApiProcessor = RseapiZosBatchManagerImpl.rseapiManager.newRseapiRestApiProcessor(jobImage, RseapiZosBatchManagerImpl.zosManager.getZosBatchPropertyRestrictToImage(jobImage.getImageID()));
        } catch (RseapiManagerException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }
    }
    
    public @NotNull IZosBatchJob submitJob() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("jcl", jclWithJobcard());
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.POST_JSON, RESTJOBS_PATH + SLASH + "string" + SLASH, headers, requestBody, VALID_STATUS_CODES, true);
        } catch (RseapiException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }

        if (response.getStatusCode() == HttpStatus.SC_CREATED) {
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
        
            logger.trace(responseBody);
            this.jobid = jsonNull(responseBody, PROP_JOBID);
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.retcode = jsonNull(responseBody, PROP_RETCODE);
            setJobPathValues();
            logger.info("JOB " + this + " Submitted");
        } else {            
            // Error case
            String displayMessage = buildErrorString("Submit job", response); 
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
        return (this.jobid != null ? this.jobid : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getOwner() {
        return (this.owner != null ? this.owner : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getType() {
        return (this.type != null ? this.type : StringUtils.repeat(QUERY, 3));
    }
    
    @Override
    public String getStatus() {
        return (this.status != null ? this.status : StringUtils.repeat(QUERY, 8));
    }
    
    @Override
    public String getRetcode() {
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
    public void saveOutputToTestResultsArchive() throws ZosBatchException {
        if (jobOutput() == null) {
            retrieveOutput();
        }
        Path artifactPath = RseapiZosBatchManagerImpl.getCurrentTestMethodArchiveFolder();
        String folderName = this.jobname.getName() + "_" + this.jobid + "_" + this.retcode.replace(" ", "-").replace(StringUtils.repeat(QUERY, 4), "UNKNOWN");
        artifactPath = artifactPath.resolve(RseapiZosBatchManagerImpl.zosManager.buildUniquePathName(artifactPath, folderName));
        
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
            String fileName = RseapiZosBatchManagerImpl.zosManager.buildUniquePathName(artifactPath, name.toString());
            try {
				RseapiZosBatchManagerImpl.zosManager.storeArtifact(artifactPath.resolve(fileName), spoolFile.getRecords(), ResultArchiveStoreContentType.TEXT);
			} catch (ZosManagerException e) {
				throw new ZosBatchException(e);
			}
        }
        if (isComplete()) {
        	this.jobArchived = true;
        }
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
        this.jobOutput = RseapiZosBatchManagerImpl.zosManager.newZosBatchJobOutput(this.jobname.getName(), this.jobid);
        this.jobFilesPath = RESTJOBS_PATH + SLASH + this.jobname.getName() + SLASH + this.jobid + "/files";
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, this.jobFilesPath, headers, null, VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {   
            Object responseBodyObject;
            try {
                responseBodyObject = response.getContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
            
            logger.trace(responseBodyObject);
            // Get the spool files
            JsonArray jsonArray;
            try {
                jsonArray = response.getJsonContent().getAsJsonArray("items");
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject responseBody = jsonElement.getAsJsonObject();
                String id = jsonNull(responseBody, PROP_ID);
                String stepname = jsonNull(responseBody, "step name");
                String procstep = jsonNull(responseBody, "proc step");
                String ddname = responseBody.get("ddName").getAsString();
                this.jobOutput.addSpoolFile(stepname, procstep, ddname, getOutputFileContent(this.jobFilesPath + "/" + id + "/content"));
            }
        } else {            
            // Error case
            String displayMessage = buildErrorString("Retrieve job output", response); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        
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
    
    protected void setStatus(String status) {
        this.status = status;
    }

    protected void cancel(boolean purge) throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            if (purge) {
                response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.DELETE, this.jobPath, headers, null, VALID_STATUS_CODES, true);
            } else {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("request", "cancel");
                response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.PUT_JSON, this.jobPath, headers, requestBody, VALID_STATUS_CODES, true);
            }
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }

        if (response.getStatusCode() == HttpStatus.SC_OK || response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            this.status = null;
            if (purge) {
                this.jobPurged = true; 
            } else {
                this.jobComplete = true;
            }
        } else {            
            // Error case
            String displayMessage = buildErrorString(purge ? "Purge job" : "Cancel job", response);
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
        return this.jobid != null;
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
        return "JOBID=" + getJobId() + 
              " JOBNAME=" + this.jobname.getName() + 
              " OWNER=" + getOwner() + 
              " TYPE=" + getType() +
              " STATUS=" + getStatus() + 
              " RETCODE=" + getRetcode();
    }

    protected void setJobPathValues() {
        this.jobPath = RESTJOBS_PATH + SLASH + this.jobname.getName() + SLASH + this.jobid;
        this.jobFilesPath = this.jobPath + "/files";
    }

    protected void updateJobStatus() throws ZosBatchException {
        if (!submitted()) {
            throw new ZosBatchException(LOG_JOB_NOT_SUBMITTED);
        }
        HashMap<String, String> headers = new HashMap<>();
        
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, RESTJOBS_PATH + SLASH + this.jobname.getName() + "/" + this.jobid, headers, null, VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
        
            logger.trace(responseBody);
            this.jobNotFound = false;
            this.owner = jsonNull(responseBody, PROP_OWNER);
            this.type = jsonNull(responseBody, PROP_TYPE);
            this.status = jsonNull(responseBody, "status");
            if (this.status != null && "COMPLETION".equals(this.status) ||
            	this.status != null && "ABEND".equals(this.status)) {
                this.jobComplete = true;
            }
            String retcodeProperty = jsonNull(responseBody, PROP_RETCODE);
            if (retcodeProperty != null) {
                this.retcode = retcodeProperty;
            } else {
                this.retcode = StringUtils.repeat(QUERY, 4);
            }
            logger.debug(jobStatus());
        } else {        
            // Error case
            String displayMessage = buildErrorString("Update job status", response); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }            
    }

    protected String getOutputFileContent(String path) throws ZosBatchException {
    
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            response = this.rseapiApiProcessor.sendRequest(RseapiRequestType.GET, path, headers, null, VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }
    
        String fileOutput;
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            JsonObject responseBody;
            try {
                responseBody = response.getJsonContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
        
            logger.debug(responseBody);
            fileOutput = jsonNull(responseBody, PROP_CONTENT);
        } else {            
            // Error case
            String displayMessage = buildErrorString("Retrieve job output", response);
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        return fileOutput;
    }

    protected boolean spoolFileNotFound(JsonObject errorResponseBody) {
        return (jsonZero(errorResponseBody, PROP_CATEGORY) == 6 &&
                jsonZero(errorResponseBody, PROP_RC) == 4 &&
                jsonZero(errorResponseBody, PROP_REASON) == 12);
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
    
    protected static String buildErrorString(String action, IRseapiResponse response) {
    	String message = "";
    	try {
    		JsonObject content = response.getJsonContent();
			if (content != null) {
				message = "\nstatus: " + content.get("status").getAsString() + "\n" + "message: " + content.get("message").getAsString(); 
			}
		} catch (RseapiException e) {
			// NOP
		}
        return "Error " + action + ", HTTP Status Code " + response.getStatusCode() + " : " + response.getStatusLine() + message;
    }

    protected void archiveJobOutput() throws ZosBatchException {
        if (!isArchived() || !this.jobComplete) {
            saveOutputToTestResultsArchive();
        }
    }
}
