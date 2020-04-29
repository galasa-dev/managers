/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix;

import javax.validation.constraints.NotNull;

/**
 * Provides the test code access to zOS UNIX via the zOS Manager 
 *
 */
public interface IZosUNIX {
    
    /**
     * Issue a zOS UNIX command 
     * 
     * @param command The command
     * @return {@link IZosUNIXCommand} A representation of the UNIX command response
     * @throws ZosUNIXCommandException 
     */
    @NotNull
    public IZosUNIXCommand issueCommand(@NotNull String command) throws ZosUNIXCommandException;
    
    /**
     * Issue a zOS UNIX command 
     * 
     * @param command The command
     * @param timeout time (in milliseconds) to wait with no new output appearing before timing out
     * @return {@link IZosUNIXCommand} A representation of the UNIX command response
     * @throws ZosUNIXCommandException 
     */
    @NotNull
    public IZosUNIXCommand issueCommand(@NotNull String command, long timeout) throws ZosUNIXCommandException;

}
