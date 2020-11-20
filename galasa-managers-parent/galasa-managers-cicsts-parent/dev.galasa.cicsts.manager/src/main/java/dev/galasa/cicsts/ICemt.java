/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.cicsts;

import java.util.HashMap;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.ITerminal;

public interface ICemt {


    /** 
     * Inquire a CEMT resource using the resource type and name.
     * This does not support inquiries of multiple resources at once. 
     * Will return {@link null} if the resource is not found.
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @return a {@link HashMap} object containing all of the properties of the resource.
     * @throws CemtException if resource not found.
     */

    

    public CicstsHashMap inquireResource(@NotNull ICicsTerminal cemtTerminal,
                                                   @NotNull String resourceType,
                                                   @NotNull String resourceName
                                                   ) throws CemtException;
    

    /** 
     * Set the state of a CEMT resource using the resource type and name. If the searchText is not found on the terminal screen then
     * an exception will be thrown.
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param resourceType a {@link String} of the type of resource you want to set.
     * @param resourceName a {@link String} of the name of the resource you want to set.
     * @param action a {@link String} of the action you want to perform.
     * @param searchText a {@link String} of the text you want to search for on the terminal screen.
     * @throws CemtException
     */

    public CicstsHashMap setResource(@NotNull ICicsTerminal cemtTerminal,
                            @NotNull String resourceType,
                                     String resourceName,
                            @NotNull String action)throws CemtException;
    
    
    /**
     * Discards a specified resource and throws an exception if the specified search text is not found on the terminal.
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param resourceType a {@link String} of the type of resource you want to discard.
     * @param resourceName a {@link String} of the name of the resource you want to discard.
     * @throws CemtException
     */

    public CicstsHashMap discardResource(@NotNull ICicsTerminal cemtTerminal,
                               @NotNull String resourceType,
                               @NotNull String resourceName) throws CemtException;
    

    
    /**
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param systemArea a {@link String} the specifies the system area.
     * @param setRequest
     * @param expectedResponse
     * @return boolean
     * @throws CemtException
     */

    public boolean performSystemProperty(@NotNull ICicsTerminal cemtTerminal,
                                         @NotNull String systemArea,
                                         @NotNull String setRequest,
                                         @NotNull String expectedResponse)throws CemtException;
}
