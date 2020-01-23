package dev.galasa.kubernetes;

import java.util.List;

public interface IReplicaSet extends IResource {

    public List<IPodLog> getPodLogs(String container) throws KubernetesManagerException;

}
