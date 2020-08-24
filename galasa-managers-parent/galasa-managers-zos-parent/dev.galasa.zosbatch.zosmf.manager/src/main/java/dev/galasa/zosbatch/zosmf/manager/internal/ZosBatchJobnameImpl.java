/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;

/**
 * Implementation of {@link IZosBatchJobname} using zOS/MF
 *
 */
public class ZosBatchJobnameImpl implements IZosBatchJobname {
    
    private String name;

    private String jobNamePrefix;
        
    public ZosBatchJobnameImpl(@NotNull String imageId) throws ZosBatchException {

        try {
            jobNamePrefix = ZosBatchManagerImpl.zosManager.getZosBatchPropertyJobnamePrefix(imageId);
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
    
    protected ZosBatchJobnameImpl() throws ZosBatchException {}
    
    protected void setName(String name) {
        this.name = name;
    }
}
