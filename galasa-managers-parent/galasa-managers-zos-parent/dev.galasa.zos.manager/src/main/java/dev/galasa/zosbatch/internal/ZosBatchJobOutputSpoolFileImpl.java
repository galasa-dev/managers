/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.internal;

import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;

/**
 * Implementation of {@link IZosBatchJobOutputSpoolFile}
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
     * @param jobname 
     * @param jobid 
     */
    public ZosBatchJobOutputSpoolFileImpl(String jobname, String jobid, String stepname, String procstep, String ddname, String records) {

        this.jobname = jobname;
        this.jobid = jobid;
        this.stepname = stepname;
        this.procstep = procstep;
        this.ddname = ddname;
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
        return "JOB=" + jobname + " JOBID=" + jobid + " STEP=" + stepname +  " PROCSTEP=" + procstep + " DDNAME=" + ddname;
    }
}
