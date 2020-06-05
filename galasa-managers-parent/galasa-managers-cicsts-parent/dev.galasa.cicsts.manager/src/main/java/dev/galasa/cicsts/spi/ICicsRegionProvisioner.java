/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;

public interface ICicsProvisioner {

    ICicsProvisioned provision(@NotNull String tag) throws ManagerException;

}
