/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.kubernetes;

import java.util.List;

/**
 * Represents a resource that utilises ReplicatSets,  ie Deployment and StatefulSet
 * 
 * @author mikebyls
 *
 */
public interface IReplicaSet extends IResource {

    public List<IPodLog> getPodLogs(String container) throws KubernetesManagerException;

}
