package dev.voras.common.zosbatch;

import javax.validation.constraints.NotNull;

import dev.voras.common.zos.IZosImage;

/**
 * Provides the test code access to the zOS Batch Manager 
 * 
 * @author Michael Baylis
 *
 */
public interface IZosBatchManager {
	
	/**
	 * Submit a job.
	 * 
	 * @param jcl - The JCL to submit.   Must not include the JOB statement
	 * @param jobname - {@link IJobname} A provisioned jobname, if null, a new unique jobname will be provisioned.
	 * @param image - {@link IZosImage} The zOS image the job is to run on
	 * @return {@link IBatchJob} A representation of the batchjob
	 * @throws ZosBatchException
	 */
	@NotNull
	IBatchJob submitJob(@NotNull String jcl, IJobname jobname, @NotNull IZosImage image) throws ZosBatchException;

}
