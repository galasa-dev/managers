/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.spi;

import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapiManager;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * Provides the SPI access to the RSE API Manager
 *
 */
public interface IRseapiManagerSpi extends IRseapiManager {
    
    /**
     * Returns a RSE API server for a single image
     * @param image requested image
     * @return the RSE API server
     * @throws RseapiManagerException
     */
    public IRseapi newRseapi(IZosImage image) throws RseapiManagerException;
    
    /**
     * Returns a map of RSE API servers for a cluster
     * @param clusterId the cluster id
     * @return the RSE API servers
     * @throws RseapiManagerException
     */
    public Map<String, IRseapi> getRseapis(@NotNull String clusterId) throws RseapiManagerException;

    /**
     * Returns a {@link IRseapiRestApiProcessor} for a single image
     * @param image
     * @param restrictToImage
     * @return {@link IRseapiRestApiProcessor}
     * @throws RseapiManagerException
     */
    public IRseapiRestApiProcessor newRseapiRestApiProcessor(IZosImage image, boolean restrictToImage) throws RseapiManagerException;
}
