/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.ITerminal;

public interface ICeda {
   
    public void createResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName, @NotNull String resourceParameters) throws CedaException;
      
      public void installGroup(@NotNull ITerminal terminal, @NotNull String groupName) throws CedaException;
      
      public void installResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String cedaGroup) throws CedaException;
      
      public void deleteGroup(@NotNull ITerminal terminal, @NotNull String groupName) throws CedaException;
      
      public void deleteResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName) throws CedaException;


}
