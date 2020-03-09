/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch;

/**
 * <p>Represents a privovision zOS Batch Jobname</p>
 * 
 * <p>Use a {@link ZosBatchJobname} annotation to populate this field with</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IZosBatchJobname {
 
    /**
     * Get the name of the zOS batch Jobname
     * @return String
     */
    public String getName();
}
