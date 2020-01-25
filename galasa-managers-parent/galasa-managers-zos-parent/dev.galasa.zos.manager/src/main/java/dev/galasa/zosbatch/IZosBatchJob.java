/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch;

/**
 * <p>Represents a zOS Batch Job.</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IZosBatchJob {
    
    /**
     * The {@link IZosBatchJobname} associated with this job
     * 
     * @return batch job name
     */
    public IZosBatchJobname getJobname();
    
    /**
     * The jobid for this Job. Returns "????????" if no jobid has been associated
     * 
     * @return batch jobid
     */
    public String getJobId();
    
    /**
     * The batch job status. Returns "????????" if the job has not been submitted
     * 
     * @return batch job status
     */
    public String getStatus();
    
    /**
     * The batch job completion return code. Returns "????" if the job has not been submitted
     * 
     * @return
     */
    public String getRetcode();
    
    /**
     * Wait for a job to complete. Return the highest return code for the job. The method will wait for the default 
     * resource wait time before timing out. Returns {@link Integer.MIN_VALUE} if return code is non numeric. 
     * Use {@link #getRetcode()} to get the {@link String} value
     * 
     * @return highest CC
     * @throws ZosBatchException
     */
    public int waitForJob() throws ZosBatchException;

    /**
     * Retrieve all the output of the batch job
     * 
     * @return Lines of output
     * @throws ZosBatchException
     */
    public IZosBatchJobOutput retrieveOutput() throws ZosBatchException;

    /**
     * Purge the batch job from the queue
     * 
     * @throws ZosBatchException
     */
    public void purgeJob() throws ZosBatchException;

}
