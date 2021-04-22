/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2020,2021.
 */
package dev.galasa.zosbatch;

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
     * @return the zOS batch job DD name
     */
    public String getDdname();

    /**
     * Return the id associated with this zOS batch job spool file
     * @return the zOS batch job spool file ID
     */
	public String getId();
    
    /**
     * Retrieve current content of spool file from zOS
     */
    public void retrieve() throws ZosBatchException;
    
    /**
     * Return the content of this zOS batch job spool file
     * @return the zOS batch job spool file content
     */
    public String getRecords();

}
