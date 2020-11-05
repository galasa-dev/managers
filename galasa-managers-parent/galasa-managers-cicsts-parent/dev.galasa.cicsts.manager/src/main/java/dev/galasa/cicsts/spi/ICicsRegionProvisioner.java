/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ResourceUnavailableException;

public interface ICicsRegionProvisioner {

    void cicsProvisionGenerate() throws ManagerException, ResourceUnavailableException;
    
    void cicsProvisionBuild() throws ManagerException, ResourceUnavailableException;
    
    ICicsRegionProvisioned provision(@NotNull String cicsTag, @NotNull String imageTag, @NotNull List<Annotation> annotations) throws ManagerException;

    void provisionStart() throws ManagerException, ResourceUnavailableException;
    
    void provisionStop();
    
    void provisionDiscard();
    
    void shutdown();
}
