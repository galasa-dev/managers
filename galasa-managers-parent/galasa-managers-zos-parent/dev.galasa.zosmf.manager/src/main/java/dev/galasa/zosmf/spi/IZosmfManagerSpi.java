package dev.galasa.zosmf.spi;

import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmfManager;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfManagerException;

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

	/**
	 * Returns a {@link IZosmfRestApiProcessor} for a single image
	 * @param image
	 * @param restrictToImage
	 * @return {@link IZosmfRestApiProcessor}
	 * @throws ZosmfManagerException
	 */
	public IZosmfRestApiProcessor newZosmfRestApiProcessor(IZosImage image, boolean restrictToImage) throws ZosmfManagerException;
}
