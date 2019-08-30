package dev.galasa.common.zosbatch;

import javax.validation.constraints.NotNull;

/**
 * Provides the test code access to the zOS Batch Manager 
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
	 * @throws ZosBatchManagerException 
	 */
	@NotNull
	public IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname) throws ZosBatchException;

}
