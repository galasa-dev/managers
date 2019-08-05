package dev.voras.common.zosbatch;

import java.util.Map;

/**
 * <p>Represents a zOS Batch Job output</p>
 *
 */
public interface IZosBatchJobOutput {
	
	String getJcl() throws ZosBatchException;

	String getJobname() throws ZosBatchException;

	String getJobid() throws ZosBatchException;

	Map<String, String> getOutput();

	String[] toArray();

}
