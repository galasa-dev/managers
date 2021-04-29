/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.galasaecosystem;

import javax.validation.constraints.NotNull;

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
    
    /**
     * @param endpoint {@link EcosystemEndpoint} Which endpoint is required
     * @return Object of the endpoint, never null, URL, URI or InetSocketAddress
     * @throws GalasaEcosystemManagerException If the endpoint is unsupported
     */
    public @NotNull Object getEndpoint(@NotNull EcosystemEndpoint endpoint) throws GalasaEcosystemManagerException;
        
    public JsonObject getSubmittedRuns(String groupName) throws GalasaEcosystemManagerException;

    public JsonObject waitForGroupNames(String groupName, long timeout) throws GalasaEcosystemManagerException; 
    
}