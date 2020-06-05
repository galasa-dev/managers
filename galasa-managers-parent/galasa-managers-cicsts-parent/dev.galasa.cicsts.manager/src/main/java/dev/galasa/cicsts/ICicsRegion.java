/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

import dev.galasa.ProductVersion;
import dev.galasa.zos.IZosImage;

public interface ICicsRegion {

    /***
     * Retrieve the CICS TS Region tag
     * @return the tag of the CICS TS Region
     */
    String getTag();

    /***
     * Retrieve the CICS TS Region applid
     * @return the applid of the CICS TS Region
     * @throws CicstsManagerException If the applid is not available
     */
    String getApplid();

    /***
     * Retrieve the CICS TS Region version
     * @return the version of the CICS TS Region
     * @throws CicstsManagerException If the version is not available
     */
    ProductVersion getVersion() throws CicstsManagerException;
    
    /***
     * Retrieve the zOS Image the CICS TS region resides on
     * @return the zOS Image the CICS TS region resides on
     */
    IZosImage getZosImage();
}