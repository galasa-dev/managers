/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchJobOutputSpoolFileImpl;

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
        Assert.assertEquals("getJobname() should return the supplied value", JOBNAME, zosBatchJobOutputSpoolFile.getJobname());
        Assert.assertEquals("getJobid() should return the supplied value", JOBID, zosBatchJobOutputSpoolFile.getJobid());
        Assert.assertEquals("getStepname() should return an empty String", "", zosBatchJobOutputSpoolFile.getStepname());
        Assert.assertEquals("getProcstep() should return an empty String", "", zosBatchJobOutputSpoolFile.getProcstep());
        Assert.assertEquals("getDdname() should return 'JESJCLIN'", "JESJCLIN", zosBatchJobOutputSpoolFile.getDdname());
        Assert.assertEquals("getRecords() should return the supplied value", RECORDS, zosBatchJobOutputSpoolFile.getRecords());
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
        Assert.assertEquals("getJobname() should return the supplied value", JOBNAME, zosBatchJobOutputSpoolFile.getJobname());
        Assert.assertEquals("getJobid() should return the supplied value", JOBID, zosBatchJobOutputSpoolFile.getJobid());
        Assert.assertEquals("getStepname() should return the supplied value", STEPNAME, zosBatchJobOutputSpoolFile.getStepname());
        Assert.assertEquals("getProcstep() should return the supplied value", PROCSTEP, zosBatchJobOutputSpoolFile.getProcstep());
        Assert.assertEquals("getDdname() should return the supplied value", DDNAME, zosBatchJobOutputSpoolFile.getDdname());
        Assert.assertEquals("getRecords() should return the supplied value", RECORDS, zosBatchJobOutputSpoolFile.getRecords());
        
        Assert.assertEquals("toString() should return the values of JOBNAME JOBID STEPNAME PROCSTEP DDNAME", JOBNAME + " " + JOBID + " " + STEPNAME +  " " + PROCSTEP + " " + DDNAME, zosBatchJobOutputSpoolFile.toString());
    }
    
    @Test
    public void testJsonNull() {
        ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(JOBNAME, JOBID, RECORDS);
        
        JsonObject jsonObject = new JsonObject();
        Assert.assertEquals("jsonNull() should return an empty String", "", zosBatchJobOutputSpoolFile.jsonNull(jsonObject, DUMMY));
        
        jsonObject.add(DUMMY, JsonNull.INSTANCE);
        Assert.assertEquals("jsonNull() should return an empty String", "", zosBatchJobOutputSpoolFile.jsonNull(jsonObject, DUMMY));
        
        jsonObject.addProperty(JOBNAME, JOBNAME);
        Assert.assertEquals("jsonNull() should return an empty String", JOBNAME, zosBatchJobOutputSpoolFile.jsonNull(jsonObject, JOBNAME));        
    }
}
