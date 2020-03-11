/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch;

import javax.validation.constraints.NotNull;

/**
 * Provides the test code access to zOS Batch jobs via the zOS Manager 
 * 
 * @author Michael Baylis
 *
 */
public interface IZosBatch {
    
    /**
     * Submit a job.
     * 
     * @param jcl - The JCL to submit.   Must not include the JOB statement
     * @param jobname - {@link IZosBatchJobname} A provisioned jobname, if null, a new unique jobname will be provisioned.
     * @return {@link IZosBatchJob} A representation of the batchjob
     * @throws ZosBatchException 
     */
    @NotNull
    public IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname) throws ZosBatchException;

    /**
     * Submit a job.
     * 
     * @param jcl - The JCL to submit.   Must not include the JOB statement
     * @param jobname - {@link IZosBatchJobname} A provisioned jobname, if null, a new unique jobname will be provisioned.
     * @Param jobcard - {@link IZosBatchJobcard} Overrides for the job card values
     * @return {@link IZosBatchJob} A representation of the batchjob
     * @throws ZosBatchException 
     */
    @NotNull
    public IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, ZosBatchJobcard jobCard) throws ZosBatchException;

}
