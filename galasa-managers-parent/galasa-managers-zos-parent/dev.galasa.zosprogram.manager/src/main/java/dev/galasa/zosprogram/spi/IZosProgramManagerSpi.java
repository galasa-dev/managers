/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * Provides the SPI access to the zOS Program Manager
 *
 */
public interface IZosProgramManagerSpi {
    /**
     * Returns a zOS Program compiled and linked on a single image
     * @param image The zOS Image
     * @param name The program name
     * @param location Path to the location of the program source in the bundle. This can be either the full path including the file name
     * or the directory containing the source with the name specified in the name attribute with the extension specified in the language attribute.
     * @param language The programming language. See {@link ZosProgram.Language}
     * @param isCics Is a CICS program.
     * @param loadlib The load module data set name
     * @return The zOS Program
     * @throws ZosProgramManagerException
     */
    public IZosProgram newZosProgram(@NotNull IZosImage image, @NotNull String name, @NotNull String location, @NotNull Language language, boolean isCics, String loadlib) throws ZosProgramManagerException;
}
