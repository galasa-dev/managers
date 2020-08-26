/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchJobOutputSpoolFileImpl { 

    private static final String JOBNAME = "jobname";

    private static final String JOBID = "jobid";
    
    private static final String STEPNAME = "stepname";
    
    private static final String PROCSTEP = "procstep";
    
    private static final String DDNAME = "ddname";

    private static final String RECORDS = "records";
    
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
        ZosBatchJobOutputSpoolFileImpl zosBatchJobOutputSpoolFile = new ZosBatchJobOutputSpoolFileImpl(JOBNAME, JOBID, STEPNAME, PROCSTEP, DDNAME, RECORDS);
        Assert.assertEquals("getJobname() should return the supplied value", JOBNAME, zosBatchJobOutputSpoolFile.getJobname());
        Assert.assertEquals("getJobid() should return the supplied value", JOBID, zosBatchJobOutputSpoolFile.getJobid());
        Assert.assertEquals("getStepname() should return the supplied value", STEPNAME, zosBatchJobOutputSpoolFile.getStepname());
        Assert.assertEquals("getProcstep() should return the supplied value", PROCSTEP, zosBatchJobOutputSpoolFile.getProcstep());
        Assert.assertEquals("getDdname() should return the supplied value", DDNAME, zosBatchJobOutputSpoolFile.getDdname());
        Assert.assertEquals("getRecords() should return the supplied value", RECORDS, zosBatchJobOutputSpoolFile.getRecords());
        
        String expectedString = "JOB=" + JOBNAME + " JOBID=" + JOBID + " STEP=" + STEPNAME +  " PROCSTEP=" + PROCSTEP + " DDNAME=" + DDNAME;
        Assert.assertEquals("toString() should return the values of JOBNAME JOBID STEPNAME PROCSTEP DDNAME", expectedString , zosBatchJobOutputSpoolFile.toString());
    }
}
