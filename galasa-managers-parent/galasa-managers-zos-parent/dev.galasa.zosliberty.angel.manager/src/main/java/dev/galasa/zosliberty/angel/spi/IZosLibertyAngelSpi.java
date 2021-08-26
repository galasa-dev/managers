/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zosliberty.angel.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zosliberty.angel.IZosLibertyAngel;
import dev.galasa.zosliberty.angel.ZosLibertyAngelManagerException;

/**
 * SPI interface to {@link IZosLibertyAngel}
 */
public interface IZosLibertyAngelSpi {

    /**
     * Returns a zOS Liberty Angel instance
     * @return a {@link IZosLibertyAngel} implementation instance
     * @throws ZosLibertyAngelManagerException
     */
    @NotNull
    public IZosLibertyAngel newZosLibertyAngel() throws ZosLibertyAngelManagerException;
}
