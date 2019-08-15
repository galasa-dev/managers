package dev.galasa.common.zosbatch.zosmf.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.common.zosbatch.IZosBatchJobname;
import dev.galasa.common.zosbatch.ZosBatchException;
import dev.galasa.common.zosbatch.ZosBatchManagerException;
import dev.galasa.common.zosbatch.zosmf.internal.properties.JobnamePrefix;

public class ZosBatchJobnameImpl implements IZosBatchJobname {
	
	private String name;

	private String jobNamePrefix;
	
	public ZosBatchJobnameImpl(@NotNull @NotNull String imageId) throws ZosBatchException {

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
