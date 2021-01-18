/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosconsole.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleManagerException;

/**
 * SPI interface to {@link IZosConsole}
 */
public interface IZosConsoleSpi {

    /**
     * Returns a zOS Console instance
     * @param image zOS Image
     * @return an {@link IZosConsole} implementation instance
     * @throws ZosConsoleManagerException 
     */
    @NotNull
    public IZosConsole getZosConsole(IZosImage image) throws ZosConsoleManagerException;
}
