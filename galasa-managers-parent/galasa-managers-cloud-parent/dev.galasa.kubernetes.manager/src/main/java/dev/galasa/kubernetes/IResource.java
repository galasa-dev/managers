package dev.galasa.kubernetes;

public interface IResource {

    public enum TYPE {
        Deployment,
        StatefulSet,
        Secret,
        Service,
        ConfigMap,
        PersistentVolumeClaim
    }
    
    public String getName();
    public TYPE getType();
    public String getYaml();
    
    public void refresh() throws KubernetesManagerException;

}
