package dev.voras.common.zosmf.spi;

import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosmf.IZosmf;
import dev.voras.common.zosmf.IZosmfManager;
import dev.voras.common.zosmf.ZosmfManagerException;

/**
 * Provides the SPI access to the zOSMF Manager
 *
 */
public interface IZosmfManagerSpi extends IZosmfManager {
	
	/**
	 * Returns a zOSMF server for a single image
	 * @param image requested image
	 * @return the zOSMF server
	 * @throws ZosmfManagerException
	 */
	public IZosmf newZosmf(IZosImage image) throws ZosmfManagerException;
	
	/**
	 * Returns a map of zOSMF servers for a cluster
	 * @param clusterId the cluster id
	 * @return the zOSMF servers
	 * @throws ZosmfManagerException
	 */
	public Map<String, IZosmf> getZosmfs(@NotNull String clusterId) throws ZosmfManagerException;
}
