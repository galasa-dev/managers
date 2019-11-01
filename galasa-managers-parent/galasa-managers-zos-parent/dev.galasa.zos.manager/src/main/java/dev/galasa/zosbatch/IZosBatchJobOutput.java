/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch;

import java.util.List;

/**
 * Represents a zOS Batch Job output
 *
 */
public interface IZosBatchJobOutput extends Iterable<IZosBatchJobOutputSpoolFile>{

	/**
	 * Returns the zOS batch jobname
	 * 
	 * @return jobname
	 * @throws ZosBatchException
	 */
	public String getJobname() throws ZosBatchException;

	/**
	 * Returns the zOS batch jobid
	 * 
	 * @return jobid
	 * @throws ZosBatchException
	 */
	public String getJobid() throws ZosBatchException;

	/**
	 * Returns the zOS batch job spool files
	 * 
	 * @return An {@link List} of {@link IZosBatchJobOutputSpoolFile}
	 */
	public List<IZosBatchJobOutputSpoolFile> getSpoolFiles();

	/**
	 * Returns the zOS batch job spool files as a {@link List} of spool files
	 * 
	 * @return a {@Link List} of spool files 
	 */
	public List<String> toList();

}
