/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.spi;

import dev.galasa.zosbatch.IZosBatchJobOutput;

/**
 * SPI to zOS Batch Job output
 *
 */
public interface IZosBatchJobOutputSpi extends IZosBatchJobOutput {	
	/**
	 * Add a spool file to the job output
	 * @param stepname
	 * @param procstep
	 * @param ddname
	 * @param fileOutput
	 */
	public void addSpoolFile(String stepname, String procstep, String ddname, String fileOutput);

	/**
	 * Add JCL to the job output
	 * @param fileOutput
	 */
	public void addJcl(String fileOutput);

}
