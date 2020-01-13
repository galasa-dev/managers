/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.internal.properties.JobnamePrefix;

/**
 * Implementation of {@link IZosBatchJobname} using zOS/MF
 *
 */
public class ZosBatchJobnameImpl implements IZosBatchJobname {
	
	private String name;

	private String jobNamePrefix;
	
	public ZosBatchJobnameImpl(@NotNull String imageId) throws ZosBatchException {

		try {
			jobNamePrefix = JobnamePrefix.get(imageId);
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Problem getting batch jobname prefix", e);
		}
		this.name = jobNamePrefix + RandomStringUtils.randomAlphanumeric(8-jobNamePrefix.length()).toUpperCase();
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
