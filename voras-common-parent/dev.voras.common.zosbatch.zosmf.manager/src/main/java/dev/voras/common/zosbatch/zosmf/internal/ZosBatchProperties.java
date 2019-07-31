package dev.voras.common.zosbatch.zosmf.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.common.zosbatch.ZosBatchManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IFramework;

public class ZosBatchProperties {
	
	private static final Log logger = LogFactory.getLog(ZosBatchProperties.class);
	
	private final IConfigurationPropertyStoreService cps;
	
	private static final String DEFAULT_JOBNAME_PREFIX = "G";
	private static final int DEFAULT_JOB_WAIT_TIMEOUT = 5 * 60 * 100;

	public ZosBatchProperties(@NotNull IFramework framework) throws ZosBatchManagerException {
		try {
			this.cps = framework.getConfigurationPropertyService(ZosBatchManagerImpl.NAMESPACE);
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosBatchManagerException("Unable to request CPS for the z/OSMF Batch Manager", e);
		}
	}

	public String getJobnamePrefix(@NotNull @NotNull @NotNull String imageId) throws ZosBatchManagerException {
		try {
			String jobNamePrefixValue = ZosBatchManagerImpl.nulled(this.cps.getProperty("jobname", "prefix", imageId));

			if (jobNamePrefixValue == null || jobNamePrefixValue.isEmpty()) {
				return DEFAULT_JOBNAME_PREFIX;
			} else {
				String jobNamePrefix = jobNamePrefixValue.toUpperCase();
				if (jobNamePrefix.length() > 7 || !jobNamePrefix.matches("^[A-Z$#@][A-Z0-9$#@]*$")) {
					logger.warn("Invalid Batch Job prefix \"" + jobNamePrefixValue + "\". Using default value of \"" + DEFAULT_JOBNAME_PREFIX + "\"");
				}
				return jobNamePrefix;
			}
		} catch (Exception e) {
			throw new ZosBatchManagerException("Problem asking the CPS for the z/OSMF jobname prefix for zOS image "  + imageId, e);
		}
	}

	public int getDefaultJobWaitTimeout(@NotNull String imageId) throws ZosBatchManagerException {
		try {
			String timeoutString = ZosBatchManagerImpl.nulled(this.cps.getProperty("batchjob", "timeout", imageId));
			if (timeoutString == null) {
				return DEFAULT_JOB_WAIT_TIMEOUT;
			}
			return Integer.parseInt(timeoutString);
		} catch (Exception e) {
			throw new ZosBatchManagerException("Problem asking the CPS for the defaut batch job timeout for zOS image "  + imageId, e);
		}
	}

	public String getZosmfPort(@NotNull String imageId) throws ZosBatchManagerException {
		try {
			String zosmfPort = ZosBatchManagerImpl.nulled(this.cps.getProperty("zosmf", "port", imageId));
			if (zosmfPort == null) {
				throw new ZosBatchManagerException("Value for z/OSMF Port not configured");
			}
			return zosmfPort;
		} catch (Exception e) {
			throw new ZosBatchManagerException("Problem asking the CPS for the z/OSMF Port for zOS image "  + imageId, e);
		}
	}
}
