/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOS Batch jobname prefix
 * <p>
 * The 1-7 character for the zOS Batch jobname prefix when submitted via zOSMF
 * </p><p>
 * The property is:<br>
 * {@code zosbatch.jobname.[imageid].prefix=JOB} 
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_JOBNAME_PREFIX}
 * </p>
 *
 */
public class JobnamePrefix extends CpsProperties {
    
    private static final Log logger = LogFactory.getLog(JobnamePrefix.class);

    private static final String DEFAULT_JOBNAME_PREFIX = "GAL";

    public static String get(String imageId) throws ZosBatchManagerException {
        try {
            String jobNamePrefixValue = getStringNulled(ZosBatchZosmfPropertiesSingleton.cps(), "jobname", "prefix", imageId);

            if (jobNamePrefixValue == null) {
                return DEFAULT_JOBNAME_PREFIX;
            } else {
                String jobNamePrefix = jobNamePrefixValue.toUpperCase();
                if (jobNamePrefix.length() > 7 || !jobNamePrefix.matches("^[A-Z$#@][A-Z0-9$#@]*$")) {
                    logger.warn("Invalid Batch Job prefix \"" + jobNamePrefixValue + "\". Using default value of \"" + DEFAULT_JOBNAME_PREFIX + "\"");
                    return DEFAULT_JOBNAME_PREFIX;
                }
                return jobNamePrefix;
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the zOSMF jobname prefix for zOS image "  + imageId, e);
        }
    }

}
