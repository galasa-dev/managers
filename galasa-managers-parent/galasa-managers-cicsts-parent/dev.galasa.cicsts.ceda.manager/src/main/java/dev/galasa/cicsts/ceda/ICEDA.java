<<<<<<< HEAD
=======
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
package dev.galasa.cicsts.ceda;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.ITerminal;

public interface ICEDA {
   
   public void createResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
<<<<<<< HEAD
         @NotNull String resourceName, @NotNull String groupName, String resourceParameters) throws CEDAException;
=======
         @NotNull String resourceName, @NotNull String groupName, @NotNull String resourceParameters) throws CEDAException;
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
   
   public void installGroup(@NotNull ITerminal terminal, @NotNull String groupName) throws CEDAException;
   
   public void installResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String cedaGroup) throws CEDAException;
   
   public void deleteGroup(@NotNull ITerminal terminal, @NotNull String groupName) throws CEDAException;
   
   public void deleteResource(@NotNull ITerminal terminal, @NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String groupName) throws CEDAException;

}
