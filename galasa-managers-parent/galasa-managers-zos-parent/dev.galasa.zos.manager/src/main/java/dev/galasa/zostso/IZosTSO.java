/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso;

import javax.validation.constraints.NotNull;

/**
 * Provides the test code access to zOS TSO via the zOS Manager 
 *
 */
public interface IZosTSO {
    
    /**
     * Issue a zOS TSO command
     * 
     * @param command The TSO command
     * @return {@link IZosTSOCommand} A representation of the TSO command response
     * @throws ZosTSOCommandException 
     */
    @NotNull
    public IZosTSOCommand issueCommand(@NotNull String command) throws ZosTSOCommandException;
    
    /**
     * Issue a zOS TSO command
     * 
     * @param command The TSO command
     * @param timeout time (in milliseconds) to wait with no new output appearing before timing out
     * @return {@link IZosTSOCommand} A representation of the TSO command response
     * @throws ZosTSOCommandException 
     */
    @NotNull
    public IZosTSOCommand issueCommand(@NotNull String command, long timeout) throws ZosTSOCommandException;

}
