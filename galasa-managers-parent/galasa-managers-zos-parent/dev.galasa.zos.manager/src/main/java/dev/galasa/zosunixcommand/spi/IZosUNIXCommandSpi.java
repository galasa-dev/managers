/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunixcommand.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosunixcommand.IZosUNIXCommand;

/**
 * SPI interface to {@link IZosUNIXCommand}
 */
public interface IZosUNIXCommandSpi {

    /**
     * Returns a zOS UNIX Command instance
     * @param image zOS Image
     * @return an {@link IZosUNIXCommand} implementation instance
     */
    @NotNull
    public IZosUNIXCommand getZosUNIXCommand(IZosImage image);
}
