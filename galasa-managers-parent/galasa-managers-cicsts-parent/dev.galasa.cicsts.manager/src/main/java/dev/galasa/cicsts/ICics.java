/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.cicsts;

import dev.galasa.ProductVersion;

public interface ICics {

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
    String getApplid() throws CicstsManagerException;

    /***
     * Retrieve the CICS TS Region version
     * @return the version of the CICS TS Region
     * @throws CicstsManagerException If the version is not available
     */
    ProductVersion getVersion() throws CicstsManagerException;
    
}