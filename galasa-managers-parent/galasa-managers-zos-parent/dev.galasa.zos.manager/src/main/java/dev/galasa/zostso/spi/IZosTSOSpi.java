/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zostso.IZosTSO;
import dev.galasa.zostso.ZosTSOCommandManagerException;

/**
 * SPI interface to {@link IZosTSO}
 */
public interface IZosTSOSpi {

    /**
     * Returns a zOS TSO instance
     * @param image zOS Image
     * @return an {@link IZosTSO} implementation instance
     */
    @NotNull
    public IZosTSO getZosTSO(IZosImage image) throws ZosTSOCommandManagerException;
}
