package dev.galasa.common.zosbatch;

/**
 * Represents the a spool file from a zOS Batch job 
 * 
 */
public interface IZosBatchJobOutputSpoolFile {
	
	/**
	 * Return the job name associated with this zOS batch job spool file
	 * @return the zOS batch job job name
	 */
	public String getJobname();
	
	/**
	 * Return the job id associated with this zOS batch job spool file
	 * @return the zOs batch job job id
	 */
	public String getJobid();
	
	/**
	 * Return the step name associated with this zOS batch job spool file
	 * @return the zOS batch job step name
	 */
	public String getStepname();
	
	/**
	 * Return the JCL procedure step name associated with this zOS batch job spool file
	 * @return the zOS batch job proc step name or an empty {@link String}
	 */
	public String getProcstep();
	
	/**
	 * Return the DD name associated with this zOS batch job spool file
	 * @return the zOs batch job DD name
	 */
	public String getDdname();
	
	/**
	 * Return the content of this zOS batch job spool file
	 * @return the zOS batch job spool file content
	 */
	public String getRecords();

}
