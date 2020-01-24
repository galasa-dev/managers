package dev.galasa.kubernetes.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesClusterImpl;


/**
 * Kubernetes Cluster Credentials CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name kubernetes.cluster.[XXXX.]credentials
 * 
 * @galasa.description Provides the Credentials ID for the token required to access the Kubernetes Cluster
 * 
 * @galasa.required no
 * 
 * @galasa.default K8S
 * 
 * @galasa.valid_values a valid credentials ID, Galasa convention states IDs should be uppercase
 * 
 * @galasa.examples 
 * <code>kubernetes.cluster.K8S.credentials=K8S<br>
 * kubernetes.cluster.credentials=K8S</code>
 * 
 */
public class KubernetesCredentials extends CpsProperties {

    public static String get(KubernetesClusterImpl cluster) throws KubernetesManagerException {
        return getStringWithDefault(KubernetesPropertiesSingleton.cps(), "K8S", "cluster", "credentials", cluster.getId()) ;
    }
}