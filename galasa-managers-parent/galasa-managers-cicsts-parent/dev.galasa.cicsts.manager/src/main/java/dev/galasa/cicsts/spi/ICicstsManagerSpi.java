/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ProductVersion;

public interface ICicstsManagerSpi {

    void registerProvisioner(ICicsRegionProvisioner provisioner);

    @NotNull
    List<ICicsRegionLogonProvider> getLogonProviders();

    @NotNull
    String getProvisionType();

    @NotNull
    ProductVersion getDefaultVersion();

}
