package dev.voras.common.zosbatch.zosmf.internal;

import java.util.Calendar;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosbatch.IZosBatchJob;
import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatchException;
import dev.voras.common.zosbatch.ZosBatchManagerException;
import dev.voras.common.zosmf.IZosmf;
import dev.voras.common.zosmf.IZosmfResponse;
import dev.voras.common.zosmf.ZosmfException;
import dev.voras.common.zosmf.ZosmfManagerException;

public class ZosBatchJobImpl implements IZosBatchJob {
	
	private IZosBatchJobname jobname;
	private String jcl;	
	private int defaultJobTimeout;
	private IZosmf zosmf;
	
	private String jobid;			
	private String status;			
	private String retcode;
	
	private static final String RESTJOBS = "/zosmf/restjobs/jobs/";
	
	private static final Log logger = LogFactory.getLog(ZosBatchJobImpl.class);

	public ZosBatchJobImpl(IZosImage image, IZosBatchJobname jobname, String jcl) throws ZosBatchException {
		this.jobname = jobname;
		this.jcl = jcl;
		try {
			this.defaultJobTimeout = ZosBatchManagerImpl.zosBatchProperties.getDefaultJobWaitTimeout(image.getImageID());
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to create new Z/OSMF default job timeout", e);
		}
		
		try {
			this.zosmf = ZosBatchManagerImpl.zosmfManager.newZosmf(image);
		} catch (ZosmfException e) {
			throw new ZosBatchException("Unable to create new Z/OSMF object", e);
		}
	}
	
	public @NotNull IZosBatchJob submitJob() throws ZosBatchException {
		try {
			IZosmfResponse response = this.zosmf.putText(RESTJOBS, jclWithJobcard());
			logger.debug(response.getStatusLine() + " - " + response.getRequestUrl());
			if (response.getStatusCode() == 201) {
				JsonObject content = response.getJsonContent();
				
				this.jobid = content.get("jobid").getAsString();
				
				String memberName = "retcode";
				if (content.get(memberName) != null && !content.get(memberName).isJsonNull()) {
					this.retcode = content.get(memberName).getAsString();
				}
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
				if (this.status == null || this.status.equals("OUTPUT")) {
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
	public ZosBatchJobOutput retrieveOutput() throws ZosBatchException {
		
		ZosBatchJobOutput jobOutput = new ZosBatchJobOutput(this.jobname.getName(), this.jobid);
		try {
			String filesUri = RESTJOBS + this.jobname.getName() + "/" + this.jobid + "/files";
			IZosmfResponse response = this.zosmf.getJsonArray(filesUri);
			logger.debug(response.getStatusLine() + " - " + response.getRequestUrl());
			JsonArray jsonArray = response.getJsonArrayContent();
		    response = this.zosmf.getText(filesUri + "/JCL/records");
			logger.debug(response.getStatusLine() + " - " + response.getRequestUrl());
			jobOutput.setJcl(response.getTextContent());

			for (JsonElement jsonElement : jsonArray) {
			    JsonObject jsonObject = jsonElement.getAsJsonObject();
			    String id = jsonObject.get("id").getAsString();
			    response = this.zosmf.getText(filesUri + "/" + id + "/records");
				logger.debug(response.getStatusLine() + " - " + response.getRequestUrl());
				String ddname = jsonObject.get("ddname").getAsString();
				jobOutput.add(ddname, response.getTextContent());
			}
		} catch (ZosmfManagerException e) {
			throw new ZosBatchException("Problem retrieving job output from z/OSMF", e);
		}
		return jobOutput;
	}
	
	@Override
	public String getJcl() throws ZosBatchException {
		return this.jcl;
	}
	
	@Override
	public IZosBatchJobname getJobname() throws ZosBatchException {
		return this.jobname;
	}
	
	@Override
	public String toString() {
		updateJobStatus();
		return jobStatus();		
	}


	private String jobStatus() {
		return "JOBID=" + this.jobid + " JOBNAME=" + this.jobname.getName() + " STATUS=" + this.status + " RETCODE=" + (this.retcode != null ? this.retcode : "");
	}

	private void updateJobStatus() {
		try {
			IZosmfResponse response = this.zosmf.getJson(RESTJOBS + this.jobname.getName() + "/" + this.jobid);
			logger.debug(response.getStatusLine() + " - " + response.getRequestUrl());
			JsonObject content = response.getJsonContent();
			this.status = content.get("status").getAsString();
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
		return "//" + jobname.getName() + " JOB \n" + jcl;
	}

}
