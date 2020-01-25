/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.kubernetes;

import javax.validation.constraints.NotNull;

/**
 * This is the main interface to a provisioned Kubernetes Namespace on an infrastructure cluster.
 * Access to the Object is via the {@link KubernetesNamespace} annotation or the SPI. 
 *  
 * @author Michael Baylis
 *
 */
public interface IKubernetesNamespace {

    /**
     * Create a Resoource in Kubernetes.  The Manager will append certain information to the YAML before
     * creating the resource, like the run name and possibly storage classes.
     * 
     * @param yaml
     * @return
     * @throws KubernetesManagerException
     */
    @NotNull
    public IResource createResource(@NotNull String yaml) throws KubernetesManagerException;

    /**
     * @return the Full ID of the namespace in the form cluserid/namespaceid
     */
    public String getFullId();
    
    /**
     * Save all the supported resources to stored artifacts in the default folder, along with pod logs
     * 
     * @throws KubernetesManagerException if there are problems saving the entire configuration to storedartifacts
     */
    public void saveNamespaceConfiguration() throws KubernetesManagerException;
    /**
     * Save all the supported resources to stored artifacts in the default folder, along with pod logs
     * 
     * @param storedArtifactPath The path in stored artifacts to save the configuration in.  If null, will default.
     * @throws KubernetesManagerException if there are problems saving the entire configuration to storedartifacts
     */
    public void saveNamespaceConfiguration(String storedArtifactPath) throws KubernetesManagerException;



}