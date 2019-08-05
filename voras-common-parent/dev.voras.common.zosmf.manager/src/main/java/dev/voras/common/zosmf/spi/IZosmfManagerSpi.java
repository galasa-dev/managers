package dev.voras.common.zosmf.spi;

import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosmf.IZosmf;
import dev.voras.common.zosmf.IZosmfManager;
import dev.voras.common.zosmf.ZosmfException;

/**
 * Provides the SPI access to the zOS Manager
 *
 */
public interface IZosmfManagerSpi extends IZosmfManager {

	public IZosmf newZosmf(IZosImage image) throws ZosmfException;

}
