/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutput;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.RestrictToImage;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.UseSysaff;
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
public class ZosBatchJobImpl implements IZosBatchJob {
    
    IZosmfRestApiProcessor zosmfApiProcessor;
    
    private IZosImage jobImage;
    private IZosBatchJobname jobname;
    private String jcl;    
    private int jobWaitTimeout;
    
    private String jobid;            
    private String status;            
    private String retcode;
    private boolean jobComplete;
    private boolean jobArchived;
    private boolean jobPurged;
    private String jobPath;
    private String jobFilesPath;
    private ZosBatchJobOutputImpl jobOutput;
    private boolean useSysaff;

    private String uniqueId;
    
    private static final String SLASH = "/";
    private static final String RESTJOBS_PATH = SLASH + "zosmf" + SLASH + "restjobs" + SLASH + "jobs" + SLASH;
    private static final String LOG_JOB_NOT_SUBMITTED = "Job has not been submitted by manager";
    
    private static final Log logger = LogFactory.getLog(ZosBatchJobImpl.class);

    public ZosBatchJobImpl(IZosImage jobImage, IZosBatchJobname jobname, String jcl) throws ZosBatchException {
        this.jobImage = jobImage;
        this.jobname = jobname;
        this.jcl = jcl;
        storeArtifact(this.jcl, this.jobname.getName() + "_supplied_JCL.txt");
        
        try {
            this.jobWaitTimeout = JobWaitTimeout.get(this.jobImage.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Unable to get job timeout property value", e);
        }

        try {
            this.useSysaff = UseSysaff.get(this.jobImage.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Unable to get use SYSAFF property value", e);
        }
        
        try {
            this.zosmfApiProcessor = ZosBatchManagerImpl.zosmfManager.newZosmfRestApiProcessor(jobImage, RestrictToImage.get(jobImage.getImageID()));
        } catch (ZosmfManagerException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }
    }
    
    public @NotNull IZosBatchJob submitJob() throws ZosBatchException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString(), "2.0");
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_TEXT, RESTJOBS_PATH, headers, jclWithJobcard(), new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
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
        if (response.getStatusCode() == HttpStatus.SC_CREATED) {
            this.jobid = responseBody.get("jobid").getAsString();
            this.retcode = jsonNull(responseBody, "retcode");
            this.jobPath = RESTJOBS_PATH + this.jobname.getName() + SLASH + this.jobid;
            this.jobFilesPath = this.jobPath + "/files";
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
        return (this.jobid != null ? this.jobid : "????????");
    }
    
    @Override
    public String getStatus() {
        return (this.status != null ? this.status : "????????");
    }
    
    @Override
    public String getRetcode() {
        return (this.retcode != null ? this.retcode : "????");
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
            try {
                if (jobComplete()) {
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
            }
        }
        return Integer.MIN_VALUE;
    }    
    
    @Override
    public IZosBatchJobOutput retrieveOutput() throws ZosBatchException {
        if (!submitted()) {
            throw new ZosBatchException(LOG_JOB_NOT_SUBMITTED);
        }
        updateJobStatus();
        
        // First, get a list of spool files
        this.jobOutput = new ZosBatchJobOutputImpl(this.jobname.getName(), this.jobid);
        this.jobFilesPath = RESTJOBS_PATH + this.jobname.getName() + SLASH + this.jobid + "/files";
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, this.jobFilesPath, null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
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
                String id = responseBody.get("id").getAsString();
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

        archiveJobOutput();
        purgeJob();
        
        return this.jobOutput;
    }
    
    @Override
    public void purgeJob() throws ZosBatchException {
        if (!isPurged()) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put(ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString(), "2.0");
            IZosmfResponse response;
            try {
                response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, this.jobPath, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
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
                this.status = null;
                this.jobPurged = true;
            } else {            
                // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
                String displayMessage = buildErrorString("Purge job", responseBody); 
                logger.error(displayMessage);
                throw new ZosBatchException(displayMessage);
            }
        }
    }
    
    @Override
    public String toString() {
        try {
            updateJobStatus();
        } catch (ZosBatchException e) {
            logger.error(e);
        }
        return jobStatus();        
    }

    public boolean submitted() {
        return this.jobid != null;
    }
    
    public boolean jobComplete() {
        return this.jobComplete;
    }

    public boolean isArchived() {
        return this.jobArchived;
    }

    public boolean isPurged() {
        return this.jobPurged;
    }

    private String jobStatus() {
        return "JOBID=" + getJobId() + " JOBNAME=" + this.jobname.getName() + " STATUS=" + getStatus() + " RETCODE=" + getRetcode();
    }

    protected void updateJobStatus() throws ZosBatchException {
        if (!submitted()) {
            throw new ZosBatchException(LOG_JOB_NOT_SUBMITTED);
        }
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid, null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
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
            this.status = jsonNull(responseBody, "status");
            if (this.status != null && "OUTPUT".equals(this.status)) {
                this.jobComplete = true;
            }
            String retcodeProperty = jsonNull(responseBody, "retcode");
            if (retcodeProperty != null) {
                this.retcode = retcodeProperty;
            } else {
                this.retcode = "????";
            }
            logger.debug(jobStatus());
        } else {
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            String displayMessage = buildErrorString("Update job status", responseBody); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }            
    }

    protected void addOutputFileContent(JsonObject responseBody, String path) throws ZosBatchException {
    
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, path, null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
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
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            JsonObject errorResponseBody;
            try {
                errorResponseBody = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosBatchException(e);
            }
            String displayMessage = buildErrorString("Retrieve job output", errorResponseBody); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);                    
        }
        if (responseBody != null) {
            this.jobOutput.add(responseBody, fileOutput);
        } else {
            this.jobOutput.addJcl(fileOutput);
        }
    }

    protected String jclWithJobcard() {
        StringBuilder jobCard = new StringBuilder();
        jobCard.append("//");
        jobCard.append(jobname.getName());
        jobCard.append(" JOB \n");
        
        if (this.useSysaff) {
            jobCard.append("/*JOBPARM SYSAFF=");
            jobCard.append(this.jobImage.getImageID());
            jobCard.append("\n");
        }
        
        logger.error("JOBCARD:\n" + jobCard.toString());
        jobCard.append(jcl);
        return jobCard.toString();
    }

    protected String jsonNull(JsonObject responseBody, String memberName) {
        if (responseBody.get(memberName) != null && !responseBody.get(memberName).isJsonNull()) {
            return responseBody.get(memberName).getAsString();
        }
        return null;
    }

    protected String buildErrorString(String action, JsonObject responseBody) {
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
        String errorStack = responseBody.get("stack").getAsString();
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
        sb.append("\nstack:\n");
        sb.append(errorStack);
        
        return sb.toString();
    }

    protected void archiveJobOutput() throws ZosBatchException {
        if (!isArchived()) {
            if (this.jobOutput == null) {
                retrieveOutput();
                return;
            }
            String testMethodName = ZosBatchManagerImpl.currentTestMethod.getName();
            logger.info(testMethodName);
            String dirName = this.jobname.getName() + "_" + this.jobid + "_" + this.retcode.replace(" ", "-").replace("?", "X");
            logger.info("    " + dirName);
            Iterator<IZosBatchJobOutputSpoolFile> iterator = this.jobOutput.iterator();
            while (iterator.hasNext()) {
                IZosBatchJobOutputSpoolFile spoolFile = iterator.next();
                StringBuilder fileName = new StringBuilder();
                fileName.append(spoolFile.getJobname());
                fileName.append("_");
                fileName.append(spoolFile.getJobid());
                if (!spoolFile.getStepname().isEmpty()){
                    fileName.append("_");
                    fileName.append(spoolFile.getStepname());
                }
                if (!spoolFile.getProcstep().isEmpty()){
                    fileName.append("_");
                    fileName.append(spoolFile.getProcstep());
                }
                fileName.append("_");
                fileName.append(spoolFile.getDdname());
                fileName.append(".txt");
                logger.info("        " + fileName.toString());
                storeArtifact(spoolFile.getRecords(), dirName, fileName.toString());
            }
            this.jobArchived = true;
        }
    }

    protected void storeArtifact(String content, String... artifactPathElements) throws ZosBatchException {
        try {
            if (ZosBatchManagerImpl.archivePath == null) {
                throw new ZosBatchException("Unabe to get archive path");
            }
            Path artifactPath = ZosBatchManagerImpl.archivePath.resolve(ZosBatchManagerImpl.currentTestMethod.getName());
            String lastElement = artifactPathElements[artifactPathElements.length-1];
            for (String artifactPathElement : artifactPathElements) {
                if (!lastElement.equals(artifactPathElement)) {
                    artifactPath = artifactPath.resolve(artifactPathElement);
                }
            }
            int lastPeriod = StringUtils.lastIndexOf(lastElement, '.');
            String uniqueName;
            if (uniqueId == null) {
                uniqueName = lastElement;
            } else {
                uniqueName = lastElement.substring(0, lastPeriod) + uniqueId + lastElement.substring(lastPeriod);
            }
            if (Files.exists(artifactPath.resolve(uniqueName))) {
                uniqueId = "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss.SSS"));
                uniqueName = lastElement.substring(0, lastPeriod) + uniqueId + lastElement.substring(lastPeriod);
            }
            artifactPath = artifactPath.resolve(uniqueName);
            Files.createFile(artifactPath, ResultArchiveStoreContentType.TEXT);
            Files.write(artifactPath, content.getBytes()); 
        } catch (IOException e) {
            throw new ZosBatchException("Unable to store artifact", e);
        }        
    }

}
