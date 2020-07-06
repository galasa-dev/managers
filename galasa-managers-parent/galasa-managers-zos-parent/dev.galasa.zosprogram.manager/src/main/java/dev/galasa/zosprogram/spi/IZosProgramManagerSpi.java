/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.IZosProgramManager;
import dev.galasa.zosprogram.ZosProgramManagerException;
import dev.galasa.zosprogram.ZosProgram.Language;

/**
 * Provides the SPI access to the zOS Program Manager
 *
 */
public interface IZosProgramManagerSpi extends IZosProgramManager {
    //TODO : source??
    /**
     * Returns a zOS Program on a single image
     * @param image requested image?????
     * @return zOS Program
     * @throws ZosProgramManagerException
     */
    public IZosProgram newZosProgram(@NotNull IZosImage image, @NotNull String name, @NotNull Language language, @NotNull String loadlib) throws ZosProgramManagerException;
}
