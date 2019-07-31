package dev.voras.common.zosbatch.zosmf.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.voras.common.zosbatch.IZosBatchJobOutput;
import dev.voras.common.zosbatch.ZosBatchException;

public class ZosBatchJobOutput implements IZosBatchJobOutput {

	private String jobname;
	private String jobid;
	private String jcl;
	
	private Map<String, String> jobOutput = new LinkedHashMap<>();

	public ZosBatchJobOutput(String jobname, String jobid) {
		this.jobname = jobname;
		this.jobid = jobid;
	}

	public void setJcl(String jcl) {
		this.jcl = jcl;
	}

	@Override
	public String getJobname() throws ZosBatchException {
		return this.jobname;
	}

	@Override
	public String getJobid() throws ZosBatchException {
		return this.jobid;
	}

	@Override
	public String getJcl() throws ZosBatchException {
		return this.jcl;
	}
	
	@Override
	public Map<String, String> getOutput() {
		return this.jobOutput;
	}

	public void add(String id, String records) {
		this.jobOutput.put(id, records);
	}

	@Override
	public String[] toArray() {
		return this.jobOutput.values().toArray(new String[0]);
	}

	@Override
	public String toString() {
		return this.jobname + "_" + this.jobid;
	}
}
