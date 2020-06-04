/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.CicstsManagerException;

public interface ICicstsManagerSpi {

    void registerProvisioner(ICicsProvisioner provisioner);
    
    @NotNull
    List<ICicsProvisioned> getDseCicsRegions() throws CicstsManagerException;

}
