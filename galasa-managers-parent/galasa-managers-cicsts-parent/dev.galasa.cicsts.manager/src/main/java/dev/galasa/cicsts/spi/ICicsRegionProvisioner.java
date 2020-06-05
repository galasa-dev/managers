/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;

public interface ICicsRegionProvisioner {

    ICicsRegionProvisioned provision(@NotNull String tag) throws ManagerException;

}
