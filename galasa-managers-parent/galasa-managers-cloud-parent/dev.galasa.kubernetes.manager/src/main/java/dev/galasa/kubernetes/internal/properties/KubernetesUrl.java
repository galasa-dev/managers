package dev.galasa.kubernetes.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesClusterImpl;


/**
 * Kubernetes Cluster API URL CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name kubernetes.cluster.XXXX.url
 * 
 * @galasa.description The API URL of the Kubernetes Cluster
 * 
 * @galasa.required Yes
 * 
 * @galasa.default none
 * 
 * @galasa.valid_values A valid URL
 * 
 * @galasa.examples 
 * <code>kubernetes.cluster.K8S.url=http://cluster.org:8443</code>
 * 
 */
public class KubernetesUrl extends CpsProperties {

    public static String get(KubernetesClusterImpl cluster) throws KubernetesManagerException {
        try {
            return getStringNulled(KubernetesPropertiesSingleton.cps(), "cluster." + cluster.getId(), "url") ;
        } catch (ConfigurationPropertyStoreException e) {
            throw new KubernetesManagerException("Problem retrieving the url for the cluster " + cluster.getId(), e);
        }
    }
}