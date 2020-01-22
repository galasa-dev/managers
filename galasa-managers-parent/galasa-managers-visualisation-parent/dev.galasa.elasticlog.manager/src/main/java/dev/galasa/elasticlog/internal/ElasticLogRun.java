package dev.galasa.elasticlog.internal;

import java.util.Date;

public class ElasticLogRun {
	
	private String testCase;
	private String runId;
	private Date startTimestamp;
	private Date endTimestamp;
	private String result;
	
	public ElasticLogRun(String tcase, String id, Date start, Date end, String result) {
		this.testCase = tcase;
		this.runId = id;
		this.startTimestamp = start;
		this.endTimestamp = end;
		this.result = result;
	}

}
