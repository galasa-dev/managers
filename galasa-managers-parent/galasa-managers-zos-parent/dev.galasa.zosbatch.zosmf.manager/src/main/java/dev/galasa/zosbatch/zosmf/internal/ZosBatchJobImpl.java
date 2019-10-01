package dev.galasa.zosbatch.zosmf.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

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
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.ZosmfRequestType;
import dev.galasa.zosbatch.zosmf.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.zosmf.internal.properties.RequestRetry;
import dev.galasa.zosbatch.zosmf.internal.properties.UseSysaff;

/**
 * Implementation of {@link IZosBatchJob} using zOS/MF
 *
 */
public class ZosBatchJobImpl implements IZosBatchJob {
	
	private IZosImage jobImage;
	private IZosBatchJobname jobname;
	private String jcl;	
	private int jobWaitTimeout;
	private IZosmf currentZosmf;
	private String currentZosmfImageId;
	private final HashMap<String, IZosmf> zosmfs = new LinkedHashMap<>();
	
	private String jobid;			
	private String status;			
	private String retcode;
	private boolean jobComplete;
	private boolean jobArchived;
	private boolean jobPurged;
	private String jobPath;
	private String jobFilesPath;
	private ZosBatchJobOutputImpl jobOutput;
	private int retryRequest;
	private boolean useSysaff;
	
	private static final String RESTJOBS_PATH = "/zosmf/restjobs/jobs/";
	
	private static final Log logger = LogFactory.getLog(ZosBatchJobImpl.class);

	public ZosBatchJobImpl(IZosImage jobImage, IZosBatchJobname jobname, String jcl) throws ZosBatchException {
		this.jobImage = jobImage;
		this.jobname = jobname;
		this.jcl = jcl;
		storeArtifact(this.jcl, this.jobname + "_supplied_JCL.txt");
		try {
			this.jobWaitTimeout = JobWaitTimeout.get(this.jobImage.getImageID());
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to get job timeout property value", e);
		}
		try {
			this.retryRequest = RequestRetry.get(this.jobImage.getImageID());
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to get request retry property value", e);
		}

		try {
			this.useSysaff = UseSysaff.get(this.jobImage.getImageID());
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to get use SYSAFF property value", e);
		}
		
		try {
			this.zosmfs.putAll(ZosBatchManagerImpl.zosmfManager.getZosmfs(this.jobImage.getClusterID()));
		} catch (ZosManagerException e) {
			throw new ZosBatchException("Unable to create new zOSMF objects", e);
		}
		
		this.currentZosmfImageId = this.zosmfs.entrySet().iterator().next().getKey();
		this.currentZosmf = this.zosmfs.get(this.currentZosmfImageId);
	}
	
	public @NotNull IZosBatchJob submitJob() throws ZosBatchException {
		IZosmfResponse response = sendRequest(ZosmfRequestType.PUT_TEXT, RESTJOBS_PATH, jclWithJobcard(), new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED)));
		if (response == null || response.getStatusCode() == 0 || response.getStatusCode() != HttpStatus.SC_CREATED) {
			throw new ZosBatchException("Unable to submit batch job " + this.jobname.getName());
		}
		
		if (response.getStatusCode() == HttpStatus.SC_CREATED) {
			JsonObject content;
			try {
				content = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosBatchException("Unable to submit batch job " + this.jobname.getName());
			}
			
			this.jobid = content.get("jobid").getAsString();
			this.retcode = jsonNull(content, "retcode");
			this.jobPath = RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid;
			this.jobFilesPath = this.jobPath + "/files";
			logger.info("JOB " + this + " Submitted");
		}
		
		return this;
	}

	@Override
	public int waitForJob() throws ZosBatchException {
		logger.info("Waiting " + jobWaitTimeout + " seconds for "+ this.jobid + " " + this.jobname.getName() + " to complete");
		
		long timeoutTime = Calendar.getInstance().getTimeInMillis()	+ (jobWaitTimeout * 1000);
		while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
			try {
				updateJobStatus();
			} catch (Exception e) {
				throw new ZosBatchException("Unable to wait for job to complete", e);
			}
			try {
				if (this.jobComplete) {
					String[] rc = this.retcode.split(" ");
					if (rc.length > 1) {
						return StringUtils.isNumeric(rc[1]) ? Integer.parseInt(rc[1]) : 9999;
					}
					return 9999;
				}
				
				Thread.sleep(1000);
	        } catch (InterruptedException e) {
	        	logger.error("waitForJob Interrupted", e);
	        	Thread.currentThread().interrupt();
	        }
		}
		return 9999;
	}	
	
	@Override
	public IZosBatchJobOutput retrieveOutput() throws ZosBatchException {
		updateJobStatus();
		
		// First, get a list of spool files
		this.jobOutput = new ZosBatchJobOutputImpl(this.jobname.getName(), this.jobid);
		this.jobFilesPath = RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid + "/files";
		IZosmfResponse response = sendRequest(ZosmfRequestType.GET, this.jobFilesPath, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
		if (response == null || response.getStatusCode() == 0 || response.getStatusCode() != HttpStatus.SC_OK) {
			throw new ZosBatchException("Unable to retreive job output");
		}
		
		try {
			JsonArray jsonArray = response.getJsonArrayContent();
			
			// Get the JCLIN
			response = getCurrentZosmfServer().get(this.jobFilesPath + "/JCL/records", new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
			this.jobOutput.addJcl(response.getTextContent());
	
			// Get the spool files
			for (JsonElement jsonElement : jsonArray) {
			    JsonObject spoolFile = jsonElement.getAsJsonObject();
			    String id = spoolFile.get("id").getAsString();
			    response = sendRequest(ZosmfRequestType.GET, this.jobFilesPath + "/" + id + "/records", null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
				if (response == null || response.getStatusCode() == 0 || response.getStatusCode() != HttpStatus.SC_OK) {
					throw new ZosBatchException("Unable to retreive job output");
				}
				this.jobOutput.add(spoolFile, response.getTextContent());
			}
		} catch (ZosmfException e) {
			throw new ZosBatchException("Unable to retreive job output via zOSMF" + this, e);
		}

		archiveJobOutput();
		purgeJob();
		
		return this.jobOutput;
	}
	
	@Override
	public void purgeJob() throws ZosBatchException {
		if (!this.jobPurged) {
			IZosmfResponse response = sendRequest(ZosmfRequestType.DELETE, this.jobPath, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
			if (response == null || response.getStatusCode() == 0 || response.getStatusCode() != HttpStatus.SC_OK) {
				throw new ZosBatchException("Unable to purge job output");
			}
			JsonObject content;
			try {
				content = response.getJsonContent();
			} catch (ZosmfManagerException e) {
				throw new ZosBatchException("Problem purging job via zOSMF", e);
			}
			this.status = content.get("status").getAsString();			
			this.jobPurged = true;
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

	public boolean isArchived() {
		return this.jobArchived;
	}

	public boolean isPurged() {
		return this.jobPurged;
	}

	private IZosmfResponse sendRequest(ZosmfRequestType requestType, String path, Object body, List<Integer> validStatusCodes) throws ZosBatchException {
		if (validStatusCodes == null) {
			validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
		}
		IZosmfResponse response = null;
		for (int i = 0; i <= this.retryRequest; i++) {
			try {
				switch (requestType) {
				case PUT_TEXT:
					response = getCurrentZosmfServer().putText(path, (String) body, validStatusCodes);
					break;
				case PUT_JSON:
					response = getCurrentZosmfServer().putJson(path, (JsonObject) body, validStatusCodes);
					break;
				case GET:
					response = getCurrentZosmfServer().get(path, validStatusCodes);
					break;
				case DELETE:
					response = getCurrentZosmfServer().delete(path, validStatusCodes);
					break;
				default:
					throw new ZosBatchException("Invalid request type");
				}

				if (response == null || validStatusCodes.contains(response.getStatusCode())) {
			    	return response;
				} else {
					logger.error("Expected HTTP status codes: " + validStatusCodes);
			    	getNextZosmf();
				}
			} catch (ZosmfManagerException e) {
		    	logger.error(e);
		    	getNextZosmf();
			}
		}
		return response;
	}

	private IZosmf getCurrentZosmfServer() {
		logger.info("Using zOSMF on " + this.currentZosmf);
		return this.currentZosmf;
	}

	private void getNextZosmf() {
		if (this.zosmfs.size() == 1) {
			logger.debug("Only one zOSMF server available");
			return;
		}
		Iterator<Entry<String, IZosmf>> zosmfsIterator = this.zosmfs.entrySet().iterator();
		while (zosmfsIterator.hasNext()) {
			if (zosmfsIterator.next().getKey().equals(this.currentZosmfImageId)) {
				Entry<String, IZosmf> entry;
				if (zosmfsIterator.hasNext()) {
					entry = zosmfsIterator.next();
				} else {
					entry = this.zosmfs.entrySet().iterator().next();
				}
				this.currentZosmfImageId = entry.getKey();
				this.currentZosmf = this.zosmfs.get(this.currentZosmfImageId);
				return;
			}
		}
		logger.debug("No alternate zOSMF server available");
	}

	private String jobStatus() {
		return "JOBID=" + this.jobid + " JOBNAME=" + this.jobname.getName() + " STATUS=" + this.status + " RETCODE=" + (this.retcode != null ? this.retcode : "");
	}

	private void updateJobStatus() throws ZosBatchException {
		IZosmfResponse response = sendRequest(ZosmfRequestType.GET, RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
		if (response == null || response.getStatusCode() == 0 || response.getStatusCode() != HttpStatus.SC_OK) {
			return;
		}		
		JsonObject content;
		try {
			content = response.getJsonContent();
			this.status = content.get("status").getAsString();
			if (this.status == null || this.status.equals("OUTPUT")) {
				this.jobComplete = true;
			}
			String memberName = "retcode";
			if (content.get(memberName) != null && !content.get(memberName).isJsonNull()) {
				this.retcode = content.get(memberName).getAsString();
			} else {
				this.retcode = "????";
			}
			logger.debug(jobStatus());
		} catch (ZosmfException e) {
			throw new ZosBatchException("Unable to retrieve Status for job " + this);
		}
	}

	private String jclWithJobcard() {
		StringBuilder jobCard = new StringBuilder();
		jobCard.append("//");
		jobCard.append(jobname.getName());
		jobCard.append(" JOB \n");
		
		//TODO: Use JES2 member name???
		if (this.useSysaff) {
			jobCard.append("/*JOBPARM SYSAFF=");
			jobCard.append(this.jobImage.getImageID());
			jobCard.append("\n");
		}
		
		logger.debug("JOBCARD:\n" + jobCard.toString());
		jobCard.append(jcl);
		return jobCard.toString();
	}

	private String jsonNull(JsonObject content, String memberName) {
		if (content.get(memberName) != null && !content.get(memberName).isJsonNull()) {
			return content.get(memberName).getAsString();
		}
		return null;
	}

	protected void archiveJobOutput() throws ZosBatchException {
		if (!this.jobArchived) {
			if (this.jobOutput == null) {
				retrieveOutput();
				return;
			}
			String testMethodName = ZosBatchManagerImpl.currentTestMethod.getName();
			logger.info(testMethodName);
			String dirName = this.jobname + "_" + this.jobid + "_" + this.retcode.replace(" ", "-").replace("?", "X");
			logger.info("    " + dirName);
			Iterator<IZosBatchJobOutputSpoolFile> iterator = this.jobOutput.iterator();
			while (iterator.hasNext()) {
				IZosBatchJobOutputSpoolFile spoolFile = iterator.next();
				StringBuilder fileName = new StringBuilder();
				fileName.append(spoolFile.getJobname());
				fileName.append("_");
				fileName.append(spoolFile.getJobid());
				fileName.append("_");
				fileName.append(spoolFile.getProcstep().isEmpty() ? "-" : spoolFile.getProcstep());
				fileName.append("_");
				fileName.append(spoolFile.getDdname());
				fileName.append(".txt");
				logger.info("        " + fileName.toString());
				storeArtifact(spoolFile.getRecords(), dirName, fileName.toString());
			}
			this.jobArchived = true;
		}
	}

	private void storeArtifact(String content, String... artifactPathElements) throws ZosBatchException {
		try {
			Path artifactPath = ZosBatchManagerImpl.archivePath.resolve(ZosBatchManagerImpl.currentTestMethod.getName());
			for (String artifactPathElement : artifactPathElements) {
				artifactPath = artifactPath.resolve(artifactPathElement);
			}
			Files.createFile(artifactPath, ResultArchiveStoreContentType.TEXT);
			Files.write(artifactPath, content.getBytes()); 
		} catch (IOException e) {
			throw new ZosBatchException("Unable to store artifact", e);
		}		
	}

}
