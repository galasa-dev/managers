/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019-2021.
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
	 * Return the size of this zOS batch job spool file
	 * @return the zOS batch job spool file size
	 * @throws ZosBatchException 
	 */
    public long getSize() throws ZosBatchException;

	/**
     * Retrieve current content of spool file from zOS
	 * @return the size of the data retrieved
	 * @throws ZosBatchException 
     */
    public long retrieve() throws ZosBatchException;
    
    /**
     * Return the content of this zOS batch job spool file
     * @return the zOS batch job spool file content
     */
    public String getRecords();
    
    /**
     * Save this zOS batch job spool file to the Results Archive Store. Will only store records retrieved since the {@link IZosBatchJobOutputSpoolFile}
     * object was created or the last {@link #retrieve()} was issued 
     * 
     * @param rasPath path in Results Archive Store  
     * @throws ZosBatchException
     */
	public void saveToResultsArchive(String rasPath) throws ZosBatchException;

}
