/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import com.google.gson.JsonObject;

import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;

/**
 * Implementation of {@link IZosBatchJobOutputSpoolFile} using zOS/MF
 *
 */
public class ZosBatchJobOutputSpoolFileImpl implements IZosBatchJobOutputSpoolFile {

	private String jobname;
	private String jobid;
	private String stepname;
	private String procstep;
	private String ddname;
	private String records;
	
	/**
	 * Constructor for creating JESJCLIN spool file
	 * @param jobname
	 * @param jobid
	 * @param records
	 */
	public ZosBatchJobOutputSpoolFileImpl(String jobname, String jobid, String records) {

		this.jobname = jobname;
		this.jobid = jobid;
		this.stepname = "";
		this.procstep = "";
		this.ddname = "JESJCLIN";
		this.records = records;
	}
	
	/**
	 * Constructor for creating spool file
	 * @param spoolFile
	 * @param records
	 */
	public ZosBatchJobOutputSpoolFileImpl(JsonObject spoolFile, String records) {

		this.jobname = spoolFile.get("jobname").getAsString();
		this.jobid = spoolFile.get("jobid").getAsString();
		this.stepname = jsonNull(spoolFile, "stepname");
		this.procstep = jsonNull(spoolFile, "procstep");
		this.ddname = spoolFile.get("ddname").getAsString();
		this.records = records;
	}

	@Override
	public String getJobname() {
		return this.jobname;
	}


	@Override
	public String getJobid() {
		return this.jobid;
	}


	@Override
	public String getStepname() {
		return this.stepname;
	}


	@Override
	public String getProcstep() {
		return this.procstep;
	}


	@Override
	public String getDdname() {
		return this.ddname;
	}


	@Override
	public String getRecords() {
		return this.records;		
	}


	@Override
	public String toString() {
		return jobname + " " + jobid + " " + stepname +  " " + procstep + " " + ddname;
	}


	protected String jsonNull(JsonObject content, String memberName) {
		if (content.get(memberName) != null && !content.get(memberName).isJsonNull()) {
			return content.get(memberName).getAsString();
		}
		return "";
	}
}
