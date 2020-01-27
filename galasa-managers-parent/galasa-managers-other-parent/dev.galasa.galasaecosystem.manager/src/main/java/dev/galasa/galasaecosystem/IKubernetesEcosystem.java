/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.galasaecosystem;

import java.net.URI;

import javax.validation.constraints.NotNull;

/**
 * Kubernetes Ecosystem TPI
 * 
 * Provides access to the ecosystem endpoints and provides the mean to manipulate the ecosystem
 *  
 * @author Michael Baylis
 *
 */
public interface IKubernetesEcosystem {
    
    /**
     * @param endpoint {@link EcosystemEndpoint} Which endpoint is required
     * @return {@link URI} of the endpoint, never null
     * @throws GalasaEcosystemManagerException If the endpoint is unsupported
     */
    public @NotNull URI getEndpoint(@NotNull EcosystemEndpoint endpoint) throws GalasaEcosystemManagerException;
    
    /**
     * Retrieve a CPS property
     * 
     * @param property The property to retrieve
     * @return the value of the property or null if not found
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CPS
     */
    public String getCpsProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    /**
     * Set a CPS property
     * 
     * @param property The property to set
     * @param value The value to set, null means delete
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CPS
     */
    public void setCpsProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
    /**
     * Retrieve a DSS property
     * 
     * @param property The property to retrieve
     * @return the value of the property or null if not found
     * @throws GalasaEcosystemManagerException if there is a problem accessing the DSS
     */
    public String getDssProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    /**
     * Set a DSS property
     * 
     * @param property The property to set
     * @param value The value to set, null means delete
     * @throws GalasaEcosystemManagerException if there is a problem accessing the DSS
     */
    public void setDssProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
    /**
     * Retrieve a CREDS property
     * 
     * @param property The property to retrieve
     * @return the value of the property or null if not found
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CREDS
     */
    public String getCredsProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    /**
     * Set a CREDS property
     * 
     * @param property The property to set
     * @param value The value to set, null means delete
     * @throws GalasaEcosystemManagerException if there is a problem accessing the CREDS
     */
    public void setCredsProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
}