/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019-2021.
 */
package dev.galasa.zos;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.IIpHost;

/**
 * <p>Represents a zOS Image (or lpar).</p>
 * 
 * <p>Use a {@link ZosImage} annotation to populate this field with</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IZosImage {

    /**
     * Get the name of the zOS Image, may be different from the SMFID
     * 
     * @return The image ID, never null
     */
    @NotNull
    String getImageID();

    /**
     * Get the name of the SYSNAME zOS Image. Defaults to image id
     * 
     * @return The SYSNAME, never null
     */
    @NotNull
    String getSysname();
    
    /**
     * Get the name of the Sysplex this Image belongs to
     * 
     * @return the sysplex id, if the sysplexid has not been defined, the imageid will be returned
     */
    @NotNull
    String getSysplexID();
    
    /**
     * Get the name of the Cluster this Image belongs to
     * 
     *  @return a String representing the cluster the image was allocated from, if it was provisioned from a cluster
     */
    String getClusterID();

    /**
     * Get the default host name for this Image
     * 
     * @return a non-null String representing the default host name 
     * @throws ZosManagerException 
     */
    @NotNull
    String getDefaultHostname() throws ZosManagerException;

    /**
     * Retrieve the default credentials for the zOS Image. 
     * 
     * @return The default credentials - see {@link dev.galasa.framework.spi.creds.ICredentials}
     * @throws ZosManagerException if the credentials are missing or there is a problem with the credentials store
     */
    @NotNull
    ICredentials getDefaultCredentials() throws ZosManagerException;
    
    /**
     * @return The default IP Host representing the zOS Image IP Stack
     */
    @NotNull
    IIpHost getIpHost();
    
    /**
     * Get the path to the temporary zOS UNIX directory on this zOS Image for this run  
     * @return the Run Temporary UNIX Path location
     * @throws ZosManagerException
     */
    public String getRunTemporaryUNIXPath() throws ZosManagerException;

    /**
     * Get the value of Java home for the image
     * @return value of Java home
     * @throws ZosManagerException
     */
    public String getJavaHome() throws ZosManagerException;

    /**
     * Get the value of the location of the Liberty install directory
     * @return value of Liberty install directory
     * @throws ZosManagerException
     */
    public String getLibertyInstallDir() throws ZosManagerException;

    /**
     * Get the value of the location of the zOS Connect EE install directory
     * @return value of zOS Connect EE install directory
     * @throws ZosManagerException
     */
    public String getZosConnectInstallDir() throws ZosManagerException;
}
