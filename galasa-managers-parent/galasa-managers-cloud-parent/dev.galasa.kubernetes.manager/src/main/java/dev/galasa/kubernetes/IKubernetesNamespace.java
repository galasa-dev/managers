package dev.galasa.kubernetes;

import javax.validation.constraints.NotNull;

/**
 * Kubernetes Namespace 
 *  
 * @author Michael Baylis
 *
 */
public interface IKubernetesNamespace {

    @NotNull
    public IResource createResource(@NotNull String yaml) throws KubernetesManagerException;

    /**
     * @return the Full ID of the namespace in the form cluserid/namespaceid
     */
    public String getFullId();
    
    public void saveNamespaceConfiguration() throws KubernetesManagerException;
    public void saveNamespaceConfiguration(String storedArtifactPath) throws KubernetesManagerException;



}