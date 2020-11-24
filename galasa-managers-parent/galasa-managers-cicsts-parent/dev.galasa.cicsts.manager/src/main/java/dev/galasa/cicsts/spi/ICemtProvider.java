/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.ICicsRegion;

/**
 * Provides CICS Region related CEMTobjects
 *
 */
public interface ICemtProvider {
    
    /**
     * Returns a unique instance of the ICemt per CICS region 
     * 
     * @param cicsRegion
     * @return ICemt object for this CICS region, will a different instance for different regions
     */
    @NotNull
    ICemt getCemt(ICicsRegion cicsRegion) throws CicstsManagerException;

}
