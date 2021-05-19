/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.galasaecosystem;

/**
 * Kubernetes Ecosystem TPI
 * 
 * Provides access to the ecosystem endpoints and provides the mean to manipulate the ecosystem
 *  
 * @author Michael Baylis
 *
 */
public interface ILocalEcosystem extends IGenericEcosystem {
    
    
    void startSimPlatform() throws GalasaEcosystemManagerException;
    
    void stopSimPlatform() throws GalasaEcosystemManagerException;
    
}