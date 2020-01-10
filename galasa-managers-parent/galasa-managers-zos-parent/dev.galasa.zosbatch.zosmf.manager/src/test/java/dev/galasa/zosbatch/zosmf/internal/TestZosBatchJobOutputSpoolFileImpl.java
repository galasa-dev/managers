/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchJobOutputSpoolFileImpl { 

	private static final String JOBNAME = "jobname";

	private static final String JOBID = "jobid";
	
	private static final String STEPNAME = "stepname";
	
	private static final String PROCSTEP = "procstep";
	
	private static final String DDNAME = "ddname";

	private static final String RECORDS = "records";

	private static final String DUMMY = "dummy";
	
	@Test
	public void testJclConstructor() {
		ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(JOBNAME, JOBID, RECORDS);
		assertEquals(JOBNAME, zosBatchJobOutputSpoolFile.getJobname());
		assertEquals(JOBID, zosBatchJobOutputSpoolFile.getJobid());
		assertEquals("", zosBatchJobOutputSpoolFile.getStepname());
		assertEquals("", zosBatchJobOutputSpoolFile.getProcstep());
		assertEquals("JESJCLIN", zosBatchJobOutputSpoolFile.getDdname());
		assertEquals(RECORDS, zosBatchJobOutputSpoolFile.getRecords());
	}
	
	@Test
	public void testJobfileConstructor() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(JOBNAME, JOBNAME);
		jsonObject.addProperty(JOBID, JOBID);
		jsonObject.addProperty(STEPNAME, STEPNAME);
		jsonObject.addProperty(PROCSTEP, PROCSTEP);
		jsonObject.addProperty(DDNAME, DDNAME);
		
		ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(jsonObject, RECORDS);
		assertEquals(JOBNAME, zosBatchJobOutputSpoolFile.getJobname());
		assertEquals(JOBID, zosBatchJobOutputSpoolFile.getJobid());
		assertEquals(STEPNAME, zosBatchJobOutputSpoolFile.getStepname());
		assertEquals(PROCSTEP, zosBatchJobOutputSpoolFile.getProcstep());
		assertEquals(DDNAME, zosBatchJobOutputSpoolFile.getDdname());
		assertEquals(RECORDS, zosBatchJobOutputSpoolFile.getRecords());
		
		assertEquals(JOBNAME + " " + JOBID + " " + STEPNAME +  " " + PROCSTEP + " " + DDNAME, zosBatchJobOutputSpoolFile.toString());
	}
	
	@Test
	public void testJsonNull() {
		ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(JOBNAME, JOBID, RECORDS);
		
		JsonObject jsonObject = new JsonObject();
		assertEquals("", zosBatchJobOutputSpoolFile.jsonNull(jsonObject, DUMMY));
		
		jsonObject.add(DUMMY, JsonNull.INSTANCE);
		assertEquals("", zosBatchJobOutputSpoolFile.jsonNull(jsonObject, DUMMY));
		
		jsonObject.addProperty(JOBNAME, JOBNAME);
		assertEquals(JOBNAME, zosBatchJobOutputSpoolFile.jsonNull(jsonObject, JOBNAME));
		
	}
}
