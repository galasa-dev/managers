package dev.voras.common.zosbatch;

import java.util.List;

/**
 * <p>Represents a zOS Batch Job.</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IBatchJob {
	
	/**
	 * Wait for a job to complete. Return the
	 * highest return code for the job.  The method will wait for the default resource wait time before timing out.
	 * 
	 * TODO: define 'default resource wait time'
	 * 
	 * @return highest CC
	 * @throws ZosBatchException
	 */
	int waitForJob() throws ZosBatchException;

	/**
	 * Retrieve all the output of the batch job
	 * 
	 * @return Lines of output
	 * @throws ZosBatchException
	 */
	List<String> retrieveOutput() throws ZosBatchException;

}
