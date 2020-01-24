package dev.galasa.kubernetes.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.KubernetesNamespace;
import dev.galasa.kubernetes.internal.properties.KubernetesClusters;
import dev.galasa.kubernetes.internal.properties.KubernetesPropertiesSingleton;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.util.Yaml;

@Component(service = { IManager.class })
public class KubernetesManagerImpl extends AbstractManager implements IKubernetesManagerSpi {

    protected final static String               NAMESPACE = "kubernetes";
    private final Log                           logger = LogFactory.getLog(KubernetesManagerImpl.class);
    private IDynamicStatusStoreService          dss;

    private HashMap<String, KubernetesNamespaceImpl> taggedNamespaces = new HashMap<>();

    private ArrayList<KubernetesClusterImpl> clusters = new ArrayList<>();

    private boolean                             required = false;

    /**
     * Initialise the Kubernetes Manager if the Kubernetes TPI is included in the test class
     */
    @Override
    public void initialise(@NotNull IFramework framework, 
            @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, 
            @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        //*** Check to see if we are needed
        if (!required) {
            for(Field field : testClass.getFields()) {
                if (field.getType() == IKubernetesNamespace.class) {
                    required = true;
                    break;
                }
            }
        }

        if (!required) {
            return;
        }

        youAreRequired(allManagers, activeManagers);
        
        try {
            KubernetesPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new KubernetesManagerException("Failed to set the CPS with the kubernetes namespace", e);
        }

        try {
            this.dss = this.getFramework().getDynamicStatusStoreService(NAMESPACE);
        } catch(DynamicStatusStoreException e) {
            throw new KubernetesManagerException("Unable to provide the DSS for the Kubernetes Manager", e);
        }

        //*** Load the YAML supported types
        Yaml.addModelMap("v1", "ConfigMap", V1ConfigMap.class);
        Yaml.addModelMap("v1", "PersistentVolumeClaim", V1PersistentVolumeClaim.class);
        Yaml.addModelMap("v1", "Service", V1Service.class);
        Yaml.addModelMap("v1", "Secret", V1Secret.class);
        Yaml.addModelMap("v1", "Deployment", V1Deployment.class);
        Yaml.addModelMap("v1", "StatefulSet", V1StatefulSet.class);

        this.logger.info("Kubernetes Manager initialised");

    }

    /**
     * This or another manager has indicated that this manager is required
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        super.youAreRequired(allManagers, activeManagers);

        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        this.required = true;
    }

    /**
     * Generate all the annotated fields, uses the standard generate by method mechanism
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(KubernetesManagerField.class);
    }


    /**
     * Generate a Kubernetes Namespace
     * 
     * @param field The test field
     * @param annotations any annotations with the namespace
     * @return a {@link IKubernetesNamespace} namespace
     * @throws KubernetesManagerException if there is a problem generating a namespace
     */
    @GenerateAnnotatedField(annotation = KubernetesNamespace.class)
    public IKubernetesNamespace generateKubernetesNamespace(Field field, List<Annotation> annotations) throws KubernetesManagerException {
        KubernetesNamespace annotation = field.getAnnotation(KubernetesNamespace.class);

        String tag = annotation.kubernetesNamespaceTag().trim().toUpperCase();
        if (tag.isEmpty()) {
            tag = "PRIMARY";
        }

        //*** First check if we already have the tag
        IKubernetesNamespace namespace = taggedNamespaces.get(tag);
        if (namespace != null) {
            return namespace;
        }

        if (this.clusters.isEmpty()) {
            buildClusterMap();
        }

        //*** Allocate a namespace,  may take a few passes through as other tests may be trying to get namespaces at the same time

        ArrayList<KubernetesClusterImpl> availableClusters = new ArrayList<>(this.clusters);
        KubernetesNamespaceImpl allocatedNamespace = null;
        while(allocatedNamespace == null) {

            //*** Workout which cluster has the most capability
            float percentageAvailable = -1.0f;
            KubernetesClusterImpl selectedCluster = null;
            for(KubernetesClusterImpl cluster : availableClusters) {
                Float availability = cluster.getAvailability();
                if (availability != null && availability > percentageAvailable) {
                    selectedCluster = cluster;
                    percentageAvailable = availability;
                }
            }

            //*** Did we find a cluster with availability?
            if (selectedCluster == null) {
                throw new KubernetesManagerException("Unable to allocate a slot on any Kubernetes Cluster");
            }

            //*** ask cluster to allocate a namespace,  if it returns null, means we have run out of available namespaces
            //*** so try a different cluster
            allocatedNamespace = selectedCluster.allocateNamespace();
            if (allocatedNamespace == null) { // Failed to allocate namespace, remove from available clusters
                availableClusters.remove(selectedCluster);
            }
        }

        logger.info("Allocated Kubernetes Namespace " + allocatedNamespace.getId() + " on Cluster " + allocatedNamespace.getCluster().getId() + " for tag " + tag);

        taggedNamespaces.put(tag, allocatedNamespace);

        return allocatedNamespace;
    }


    @Override
    public void provisionDiscard() {

        for(KubernetesNamespaceImpl namespace : taggedNamespaces.values()) {
            try {
                namespace.discard(this.getFramework().getTestRunName());
            } catch(KubernetesManagerException e) {
                logger.error("Problem discarding namespace " + namespace.getId() + " on cluster " + namespace.getCluster().getId(), e);
            }
        }

        super.provisionDiscard();
    }

    /**
     * Build a map of all the defined clusters
     * 
     * @throws KubernetesManagerException
     */
    private void buildClusterMap() throws KubernetesManagerException {
        List<String> clusterIds = KubernetesClusters.get();

        for(String clusterId : clusterIds) {
            this.clusters.add(new KubernetesClusterImpl(clusterId, dss, getFramework()));
        }
        return;
    }

    @Override
    public IKubernetesNamespace getNamespaceByTag(@NotNull String namespaceTag) {
        String tag = namespaceTag.trim().toUpperCase();

        return taggedNamespaces.get(tag);
    }
}
