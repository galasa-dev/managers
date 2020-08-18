/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.IZosManager;
import dev.galasa.zos.ZosManagerException;

public interface IZosManagerSpi extends IZosManager {
    
    /**
     * Returns a zOS Image for the specified tag, if necessary provisions it
     * @param tag the tag of the image
     * @return and image, never null
     * @throws ZosManagerException if the tag is missing
     */
    @NotNull
    IZosImage provisionImageForTag(@NotNull String tag) throws ZosManagerException;

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

    /**
     * Returns a zOS Image for the specified image that may not have been provisioned so far
     * @param image the ID of the image
     * @return the image, never null
     * @throws ZosManagerException if there is no image defined
     */
    @NotNull
    IZosImage getUnmanagedImage(String imageId) throws ZosManagerException;

    /**
     * Returns the data set HLQ(s) for temporary data sets for the specified image
     * @param the image
     * @return the image, never null
     * @throws ZosManagerException
     */
    @NotNull
    String getRunDatasetHLQ(IZosImage image) throws ZosManagerException;

}
