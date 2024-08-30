/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.ITsq;
import dev.galasa.cicsts.TsqManagerException;
import dev.galasa.cicsts.ICicsRegion;

/**
 * Provides CICS Region related TSQ objects
 *
 */
public interface ITsqProvider {
    
    /**
     * Returns a unique instance of the ITsq per CICS region 
     * 
     * @param cicsRegion
     * @param cicstsManager
     * @return ITsq object for this CICS region, will a different instance for different regions
     * @throws TsqManagerException if getTsq() fails
     */
    @NotNull
    ITsq getTsq(ICicsRegion cicsRegion, ICicstsManagerSpi cicstsManager) throws TsqManagerException;

}