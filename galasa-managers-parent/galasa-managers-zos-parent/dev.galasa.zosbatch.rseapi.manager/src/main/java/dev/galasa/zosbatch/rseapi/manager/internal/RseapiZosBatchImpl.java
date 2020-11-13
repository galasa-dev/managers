/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.rseapi.manager.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * Implementation of {@link IZosBatch} using RSE API
 *
 */
public class RseapiZosBatchImpl implements IZosBatch {
    
    private List<RseapiZosBatchJobImpl> zosBatchJobs = new ArrayList<>();
    private IZosImage image;
    private static final Log logger = LogFactory.getLog(RseapiZosBatchImpl.class);
    
    public RseapiZosBatchImpl(IZosImage image) {
        this.image = image;
    }
    
    @Override
    public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname) throws ZosBatchException {
        return submitJob(jcl, jobname, null);
    }
    
    @Override
    public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, ZosBatchJobcard jobcard) throws ZosBatchException {
        if (jobname == null) {
            try {
				jobname = RseapiZosBatchManagerImpl.newZosBatchJobname(this.image);
			} catch (ZosBatchManagerException e) {
				throw new ZosBatchException(e);
			}
        }
        
        if (jobcard == null) {
            jobcard = new ZosBatchJobcard();
        }
        
        RseapiZosBatchJobImpl zosBatchJob = new RseapiZosBatchJobImpl(this.image, jobname, jcl, jobcard);
        this.zosBatchJobs.add(zosBatchJob);
        
        return zosBatchJob.submitJob();
    }


    @Override
    public List<IZosBatchJob> getJobs(String jobname, String owner) throws ZosBatchException {
        if (jobname != null && (jobname.isEmpty() || jobname.length() > 8)) {
            throw new ZosBatchException("Jobname must be between 1 and 8 characters or null");
        }
        if (owner != null && (owner.isEmpty() || owner.length() > 8)) {
            throw new ZosBatchException("Owner must be between 1 and 8 characters or null");
        }
        return getBatchJobs(jobname, owner);
    }

    /**
     * Clean up any existing batch jobs
     * @throws ZosBatchException
     */
    public void cleanup() throws ZosBatchException {
        
        Iterator<RseapiZosBatchJobImpl> iterator = zosBatchJobs.iterator();
        while (iterator.hasNext()) {
            RseapiZosBatchJobImpl zosBatchJobImpl = iterator.next();
            try {
				if (zosBatchJobImpl.submitted()) {
				    if (!zosBatchJobImpl.isComplete()) {
				        zosBatchJobImpl.cancel();
				        zosBatchJobImpl.archiveJobOutput();
				        zosBatchJobImpl.purge();
				    } else {
				        if (!zosBatchJobImpl.isArchived()) {
				            zosBatchJobImpl.archiveJobOutput();
				        }
				        if (!zosBatchJobImpl.isPurged()) {
				            zosBatchJobImpl.purge();
				        }
				    }
				}
			} catch (ZosBatchException e) {
				logger.error("Problem in cleanup phase", e);
			}
            iterator.remove();
        }
    }

    protected List<IZosBatchJob> getBatchJobs(String suppliedJobname, String suppliedOwner) throws ZosBatchException {
        IRseapiRestApiProcessor rseapiApiProcessor;
        try {
            rseapiApiProcessor = RseapiZosBatchManagerImpl.rseapiManager.newRseapiRestApiProcessor(image, RseapiZosBatchManagerImpl.zosManager.getZosBatchPropertyBatchRestrictToImage(image.getImageID()));
        } catch (RseapiManagerException | ZosBatchManagerException e) {
            throw new ZosBatchException(e);
        }
        String jobnameQueryString = suppliedJobname == (null) ? "prefix=*" : "prefix=" + suppliedJobname;
        String ownerQueryString = suppliedOwner == (null) ? "" : "owner=" + suppliedOwner;
        String listJobsPath = RseapiZosBatchJobImpl.RESTJOBS_PATH + "?" + jobnameQueryString + "&" + ownerQueryString;
        HashMap<String, String> headers = new HashMap<>();
        IRseapiResponse response;
        try {
            response = rseapiApiProcessor.sendRequest(RseapiRequestType.GET, listJobsPath, headers, null, RseapiZosBatchJobImpl.VALID_STATUS_CODES, true);
        } catch (RseapiException e) {
            throw new ZosBatchException(e);
        }
        
        List<IZosBatchJob> zosBatchJobList = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.SC_OK) {
            Object responseBodyObject;
            try {
                responseBodyObject = response.getContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
        
            logger.trace(responseBodyObject);
        
            // Get the jobs
            JsonArray jsonArray;
            try {
                jsonArray = response.getJsonArrayContent();
            } catch (RseapiException e) {
                throw new ZosBatchException(e);
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject responseBody = jsonElement.getAsJsonObject();
                String jobnameString = responseBody.get("jobName").getAsString();
                IZosBatchJobname jobname = RseapiZosBatchManagerImpl.newZosBatchJobname(jobnameString);
                RseapiZosBatchJobImpl zosBatchJob = new RseapiZosBatchJobImpl(this.image, jobname, null, null);
                zosBatchJob.setJobid(responseBody.get("jobID").getAsString());
                zosBatchJob.setOwner(responseBody.get("owner").getAsString());
                zosBatchJob.setType(responseBody.get("type").getAsString());
                zosBatchJob.setStatusString(responseBody.get("status").getAsString());
                zosBatchJob.setJobPathValues();
                zosBatchJobList.add(zosBatchJob);
            }
        } else {
            // Error case
            String displayMessage = RseapiZosBatchJobImpl.buildErrorString("List jobs output", response); 
            logger.error(displayMessage);
            throw new ZosBatchException(displayMessage);
        }
        return zosBatchJobList;
    }

}
