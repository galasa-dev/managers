package dev.voras.common.zosbatch.zosmf.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatchException;
import dev.voras.common.zosbatch.ZosBatchManagerException;

public class ZosBatchJobnameImpl implements IZosBatchJobname {
	
	private String name;

	private String jobNamePrefix;
	
	public ZosBatchJobnameImpl(@NotNull @NotNull String imageId) throws ZosBatchException {

		try {
			jobNamePrefix = ZosBatchManagerImpl.zosBatchProperties.getJobnamePrefix(imageId);
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
