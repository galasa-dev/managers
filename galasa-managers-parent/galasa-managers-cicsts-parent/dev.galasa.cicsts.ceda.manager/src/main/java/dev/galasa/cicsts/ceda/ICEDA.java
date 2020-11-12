
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.cicsts.ceda;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.ITerminal;

public interface ICEDA {
   
   public void createResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String groupName, String resourceParameters) throws CEDAException;

   
   public void installGroup(@NotNull ITerminal terminal, @NotNull String groupName) throws CEDAException;
   
   public void installResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String cedaGroup) throws CEDAException;
   
   public void deleteGroup(@NotNull ITerminal terminal, @NotNull String groupName) throws CEDAException;
   
   public void deleteResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String groupName) throws CEDAException;

}
