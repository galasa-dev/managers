/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 202.
 */
package dev.galasa.zosliberty.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosliberty.IZosLiberty;

/**
 * SPI interface to {@link IZosLiberty}
 */
public interface IZosLibertySpi {

    /**
     * Returns a zOS Liberty instance
     * @param image zOS Image
     * @return a {@link IZosLiberty} implementation instance
     */
    @NotNull
    public IZosLiberty getZosLiberty(@NotNull IZosImage image);
}
