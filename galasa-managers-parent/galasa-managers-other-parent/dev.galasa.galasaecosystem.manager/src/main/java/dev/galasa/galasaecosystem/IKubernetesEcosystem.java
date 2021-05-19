/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.galasaecosystem;

import com.google.gson.JsonObject;

/**
 * Kubernetes Ecosystem TPI
 * 
 * Provides access to the ecosystem endpoints and provides the mean to manipulate the ecosystem
 *  
 * @author Michael Baylis
 *
 */
public interface IKubernetesEcosystem extends IGenericEcosystem {
           
    public JsonObject getSubmittedRuns(String groupName) throws GalasaEcosystemManagerException;

    public JsonObject waitForGroupNames(String groupName, long timeout) throws GalasaEcosystemManagerException; 
    
}