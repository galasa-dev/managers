/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;

public interface ICicstsManagerSpi {

    void registerProvisioner(ICicsRegionProvisioner provisioner);

    @NotNull
    List<ICicsRegionLogonProvider> getLogonProviders();

    @NotNull
    String getProvisionType();

    @NotNull
    ProductVersion getDefaultVersion();
    
    
    /**
     * Register the a ICeci instance provider with the CICS TS Manager
     * 
     * @param ceciProvider - the new provider
     */
    void registerCeciProvider(@NotNull ICeciProvider ceciProvider);
    
    /**
     * Register the a ICeda instance provider with the CICS TS Manager
     * 
     * @param cedaProvider - the new provider
     */
    void registerCedaProvider(@NotNull ICedaProvider cedaProvider);
    
    /**
     * Register the a ICicsResource instance provider with the CICS TS Manager
     * 
     * @param cicsResourceProvider - the new provider
     */
    void registerCicsResourceProvider(@NotNull ICicsResourceProvider cicsResourceProvider);
    
    /**
     * Register the a ICemt instance provider with the CICS TS Manager
     * 
     * @param cemtProvider - the new provider
     */
    void registerCemtProvider(@NotNull ICemtProvider cemtProvider);
    
    /**
     * @return The registered CECI provider
     * @throws CicstsManagerException
     */
    @NotNull
    public ICeciProvider getCeciProvider() throws CicstsManagerException;

    /**
     * @return The registered CEDA provider
     * @throws CicstsManagerException
     */
    @NotNull
    public ICedaProvider getCedaProvider() throws CicstsManagerException;

    /**
     * @return The registered CICS Resource provider
     * @throws CicstsManagerException
     */
    @NotNull
    public ICicsResourceProvider getCicsResourceProvider() throws CicstsManagerException;

    /**
     * @return The registered CEMT provider
     * @throws CicstsManagerException
     */
    @NotNull
    public ICemtProvider getCemtProvider() throws CicstsManagerException;

    
    public void cicstsRegionStarted(ICicsRegion region) throws CicstsManagerException;

}
