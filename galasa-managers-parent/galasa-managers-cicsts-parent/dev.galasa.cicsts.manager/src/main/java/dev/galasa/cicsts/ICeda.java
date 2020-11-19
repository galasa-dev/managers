/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;

public interface ICeda {
   
   public void createResource(@NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String groupName, @NotNull String resourceParameters) throws CedaException;
   
   public void installGroup(@NotNull String groupName) throws CedaException;
   
   public void installResource(@NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String cedaGroup) throws CedaException;
   
   public void deleteGroup(@NotNull String groupName) throws CedaException;
   
   public void deleteResource(@NotNull String resourceType, 
         @NotNull String resourceName, @NotNull String groupName) throws CedaException;

}
