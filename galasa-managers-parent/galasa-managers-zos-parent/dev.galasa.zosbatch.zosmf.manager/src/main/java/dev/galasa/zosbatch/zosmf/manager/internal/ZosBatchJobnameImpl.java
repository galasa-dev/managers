/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.JobnamePrefix;

/**
 * Implementation of {@link IZosBatchJobname} using zOS/MF
 *
 */
public class ZosBatchJobnameImpl implements IZosBatchJobname {
    
    private String name;

    private String jobNamePrefix;
    private Map<String,String> parameters = new HashMap<>();
    
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

    @Override
    public void setTypeRun(TYPRUN type) {
        parameters.put("TYPRUN", type.toString());
    }

    @Override
    public String getParams() {
        if(parameters.isEmpty()){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int lineLength = 13; 

        for (String param: parameters.values()) {
            lineLength =+ param.length() + 2;
            if(lineLength > 57) {
                builder.append("\n");
                lineLength = param.length() + 2;
            }
            builder.append(param);
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length());
        return builder.toString().trim();
    }
}
