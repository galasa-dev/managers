package dev.galasa.common.zos.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.common.zos.IZosImage;
import dev.galasa.common.zos.IZosManager;
import dev.galasa.common.zos.ZosManagerException;

public interface IZosManagerSpi extends IZosManager {
	
	/**
	 * Returns a zOS Image for the specified tag
	 * @param tag the tag of the image
	 * @return and image, never null
	 * @throws ZosManagerException if the tag is missing
	 */
	@NotNull
	IZosImage getImageForTag(@NotNull String tag) throws ZosManagerException;

	/**
	 * Returns a zOS Image for the specified image ID
	 * @param image the ID of the image
	 * @return the image, never null
	 * @throws ZosManagerException
	 */
	@NotNull
	IZosImage getImage(String imageId) throws ZosManagerException;

}
