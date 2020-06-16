/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;

/**
 * SPI interface to {@link IZosFile}
 */
public interface IZosFileSpi {

    /**
     * Returns a zOS File Handler instance
     * @return an {@link IZosFileHandler} implementation instance
     */
    @NotNull
    public IZosFileHandler getZosFileHandler() throws ZosFileManagerException;
}
