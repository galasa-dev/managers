/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;

/**
 * SPI interface to {@link IZosBatch}
 */
public interface IZosBatchSpi {

    /**
     * Returns a zOS Batch instance
     * @param image zOS Image
     * @return an {@link IZosBatch} implementation instance
     */
    @NotNull
    public IZosBatch getZosBatch(@NotNull IZosImage image);
}
