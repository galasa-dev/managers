package dev.voras.common.zosbatch.zosmf.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.voras.ResultArchiveStoreContentType;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosbatch.IZosBatchJob;
import dev.voras.common.zosbatch.IZosBatchJobOutput;
import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatchException;
import dev.voras.common.zosbatch.ZosBatchManagerException;
import dev.voras.common.zosmf.IZosmf;
import dev.voras.common.zosmf.IZosmfResponse;
import dev.voras.common.zosmf.ZosmfException;
import dev.voras.common.zosmf.ZosmfManagerException;

public class ZosBatchJobImpl implements IZosBatchJob {
	
	private IZosImage image;
	private IZosBatchJobname jobname;
	private String jcl;	
	private int defaultJobTimeout;
	private IZosmf zosmf;
	
	private String jobid;			
	private String status;			
	private String retcode;
	private boolean jobComplete;
	private boolean jobArchived;
	private boolean jobPurged;
	private String jobPath;
	private String jobFilesPath;
	private ZosBatchJobOutputImpl jobOutput;
	
	private static final String RESTJOBS_PATH = "/zosmf/restjobs/jobs/";
	
	private static final Log logger = LogFactory.getLog(ZosBatchJobImpl.class);

	public ZosBatchJobImpl(IZosImage image, IZosBatchJobname jobname, String jcl) throws ZosBatchException {
		this.image = image;
		this.jobname = jobname;
		this.jcl = jcl;
		storeArtifact(this.jcl, this.jobname + "_supplied_JCL.txt");
		try {
			this.defaultJobTimeout = ZosBatchManagerImpl.zosBatchProperties.getDefaultJobWaitTimeout(this.image.getImageID());
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to create new Z/OSMF default job timeout", e);
		}
		
		try {
			this.zosmf = ZosBatchManagerImpl.zosmfManager.newZosmf(this.image);
		} catch (ZosmfException e) {
			throw new ZosBatchException("Unable to create new Z/OSMF object", e);
		}
	}
	
	public @NotNull IZosBatchJob submitJob() throws ZosBatchException {
		try {
			IZosmfResponse response = this.zosmf.putText(RESTJOBS_PATH, jclWithJobcard());
			if (response.getStatusCode() == 201) {
				JsonObject content = response.getJsonContent();
				
				this.jobid = content.get("jobid").getAsString();
				this.retcode = jsonNull(content, "retcode");
				this.jobPath = RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid;
				this.jobFilesPath = this.jobPath + "/files";
			}
		} catch (ZosmfManagerException e) {
			throw new ZosBatchException("Problem submitting job to z/OSMF", e);
		}
		
		return this;
	}
	
	
	@Override
	public int waitForJob() throws ZosBatchException {

		long timeoutTime = Calendar.getInstance().getTimeInMillis()	+ defaultJobTimeout;
		while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
			try {
				updateJobStatus();
				if (this.jobComplete) {
					String[] rc = this.retcode.split(" ");
					if (rc.length > 1) {
						return StringUtils.isNumeric(rc[1]) ? Integer.parseInt(rc[1]) : 9999;
					}
					return 9999;
				}
				
				Thread.sleep(500);
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
		this.jobOutput = new ZosBatchJobOutputImpl(this.jobname.getName(), this.jobid);
		try {
			this.jobFilesPath = RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid + "/files";
			IZosmfResponse response = this.zosmf.get(this.jobFilesPath);
			JsonArray jsonArray = response.getJsonArrayContent();
			
		    response = this.zosmf.get(this.jobFilesPath + "/JCL/records");
			this.jobOutput.addJcl(response.getTextContent());

			for (JsonElement jsonElement : jsonArray) {
			    JsonObject spoolFile = jsonElement.getAsJsonObject();
			    String id = spoolFile.get("id").getAsString();
			    response = this.zosmf.get(this.jobFilesPath + "/" + id + "/records");
				this.jobOutput.add(spoolFile, response.getTextContent());
			}
		} catch (ZosmfManagerException e) {
			throw new ZosBatchException("Problem retrieving job output from z/OSMF", e);
		}

		archiveJobOutput();
		purgeJob();
		
		return this.jobOutput;
	}
	
	@Override
	public void purgeJob() throws ZosBatchException {
		if (!this.jobPurged) {
			try {
				IZosmfResponse response = this.zosmf.deleteJson(jobPath);
				JsonObject content = response.getJsonContent();
				this.status = content.get("status").getAsString();
			} catch (ZosmfManagerException e) {
				throw new ZosBatchException("Problem purging job via z/OSMF", e);
			}
			this.jobPurged = true;
		}
	}
	
	@Override
	public String toString() {
		updateJobStatus();
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

	private void updateJobStatus() {
		try {
			IZosmfResponse response = this.zosmf.getJson(RESTJOBS_PATH + this.jobname.getName() + "/" + this.jobid);
			if (response.getStatusCode() != HttpStatus.SC_OK) {
				//TODO
				return;
			}
			JsonObject content = response.getJsonContent();
			this.status = content.get("status").getAsString();
			if (this.status == null || this.status.equals("OUTPUT")) {
				this.jobComplete = true;
			}
			String memberName = "retcode";
			if (content.get(memberName) != null && !content.get(memberName).isJsonNull()) {
				this.retcode = content.get(memberName).getAsString();
			} else {
				this.retcode = "";
			}
			logger.debug(jobStatus());
		} catch (ZosmfManagerException e) {
	    	logger.error(e);
		}
	}

	private String jclWithJobcard() {
		StringBuilder jobCard = new StringBuilder();
		jobCard.append("//");
		jobCard.append(jobname.getName());
		jobCard.append(" JOB \n");
		
		//TODO: Use JES2 member name or don't add SYSAFF
		jobCard.append("/*JOBPARM SYSAFF=");
		jobCard.append(this.image.getImageID());
		jobCard.append("\n");
		
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
			String dirName = this.jobname + "_" + this.jobid + "_" + this.retcode.replace(" ", "-");
			logger.info("    " + dirName);
			this.jobOutput.forEach(spoolFile -> {
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
			});
			this.jobArchived = true;
		}
	}

	private void storeArtifact(String content, String... artifactPathElements) {
		try {
			Path artifactPath = ZosBatchManagerImpl.archivePath.resolve(ZosBatchManagerImpl.currentTestMethod.getName());
			for (String artifactPathElement : artifactPathElements) {
				artifactPath = artifactPath.resolve(artifactPathElement);
			}
			Files.createFile(artifactPath, ResultArchiveStoreContentType.TEXT);
			Files.write(artifactPath, content.getBytes()); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
