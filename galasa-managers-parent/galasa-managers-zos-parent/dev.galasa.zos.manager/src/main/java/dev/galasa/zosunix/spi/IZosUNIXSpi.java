/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosunix.IZosUNIX;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;

/**
 * SPI interface to {@link IZosUNIX}
 */
public interface IZosUNIXSpi {

    /**
     * Returns a zOS UNIX instance
     * @param image zOS Image
     * @return an {@link IZosUNIX} implementation instance
     */
    @NotNull
    public IZosUNIX getZosUNIX(IZosImage image) throws ZosUNIXCommandManagerException;
}
