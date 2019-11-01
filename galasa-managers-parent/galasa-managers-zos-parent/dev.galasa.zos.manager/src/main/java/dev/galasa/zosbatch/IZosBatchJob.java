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
	 * Wait for a job to complete. Return the
	 * highest return code for the job.  The method will wait for the default resource wait time before timing out.
	 * 
	 * TODO: define 'default resource wait time'
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
