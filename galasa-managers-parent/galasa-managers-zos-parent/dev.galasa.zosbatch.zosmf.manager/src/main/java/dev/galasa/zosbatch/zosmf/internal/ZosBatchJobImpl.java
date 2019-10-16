package dev.galasa.zosbatch.zosmf.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import dev.galasa.zosbatch.zosmf.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.zosmf.internal.properties.RestrictToImage;
import dev.galasa.zosbatch.zosmf.internal.properties.UseSysaff;
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
	
	private static final String SLASH = "/";
	private static final String RESTJOBS_PATH = SLASH + "zosmf" + SLASH + "restjobs" + SLASH + "jobs" + SLASH;
	
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
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_TEXT, RESTJOBS_PATH, headers, jclWithJobcard(), new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED)));
		} catch (ZosmfException e) {
			throw new ZosBatchException(e);
		}
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
			this.jobPath = RESTJOBS_PATH + this.jobname.getName() + SLASH + this.jobid;
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
		this.jobFilesPath = RESTJOBS_PATH + this.jobname.getName() + SLASH + this.jobid + "/files";
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, this.jobFilesPath, null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
		} catch (ZosmfException e) {
			throw new ZosBatchException(e);
		}
		if (response == null || response.getStatusCode() == 0 || response.getStatusCode() != HttpStatus.SC_OK) {
			throw new ZosBatchException("Unable to retreive job output");
		}
		
		try {
			JsonArray jsonArray = response.getJsonArrayContent();
			
			// Get the JCLIN
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, this.jobFilesPath + "/JCL/records", null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
			this.jobOutput.addJcl(response.getTextContent());
	
			// Get the spool files
			for (JsonElement jsonElement : jsonArray) {
			    JsonObject spoolFile = jsonElement.getAsJsonObject();
			    String id = spoolFile.get("id").getAsString();
			    response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, this.jobFilesPath + "/" + id + "/records", null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
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
			HashMap<String, String> headers = new HashMap<>();
			headers.put(ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString(), "2.0");
			IZosmfResponse response;
			try {
				response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.DELETE, this.jobPath, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
			} catch (ZosmfException e) {
				throw new ZosBatchException(e);
			}
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

	private String jobStatus() {
		return "JOBID=" + this.jobid + " JOBNAME=" + this.jobname.getName() + " STATUS=" + this.status + " RETCODE=" + (this.retcode != null ? this.retcode : "");
	}

	private void updateJobStatus() throws ZosBatchException {
		IZosmfResponse response;
		try {
			response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid, null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
		} catch (ZosmfException e) {
			throw new ZosBatchException(e);
		}
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
