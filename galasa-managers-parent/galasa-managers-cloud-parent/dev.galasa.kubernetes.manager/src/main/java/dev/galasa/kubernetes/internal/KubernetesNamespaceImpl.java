package dev.galasa.kubernetes.internal;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.kubernetes.IKubernetesNamespace;

public class KubernetesNamespaceImpl implements IKubernetesNamespace {

    private final static Log                 logger = LogFactory.getLog(KubernetesNamespaceImpl.class);

    private final KubernetesClusterImpl      cluster;
    private final String                     namespaceId;
    private final IDynamicStatusStoreService dss;

    public KubernetesNamespaceImpl(KubernetesClusterImpl cluster, String namespaceId, IFramework framework, IDynamicStatusStoreService dss) {
        this.cluster     = cluster;
        this.namespaceId = namespaceId;
        this.dss         = dss;
    }

    public String getId() {
        return this.namespaceId;
    }

    public KubernetesClusterImpl getCluster() {
        return this.cluster;
    }

    public void discard(String runName) {
        cleanNamespace();
        clearSlot(runName);
    }

    private void clearSlot(String runName) {
        try {
            String namespacePrefix = "cluster." + this.cluster.getId() + ".namespace." + this.namespaceId;
            String slotKey = "slot.run." + runName + ".cluster." + this.cluster.getId() + ".namespace." + namespaceId;

            String slotStatus = dss.get(slotKey);
            if ("active".equals(slotStatus)) {
                //*** Decrement the cluster current slot count and mark the namespace as free
                while(true) { //*** have to loop around incase another test changed the current slot count
                    int currentSlots = 0;
                    String sCurrentSlots = dss.get("cluster." + this.cluster.getId() + ".current.slots");
                    if (sCurrentSlots != null) {
                        currentSlots = Integer.parseInt(sCurrentSlots);
                    }
                    currentSlots--;

                    if (currentSlots < 0) {
                        currentSlots = 0;
                    }

                    HashMap<String, String> slotOtherValues = new HashMap<>();
                    slotOtherValues.put(namespacePrefix, "free");
                    slotOtherValues.put(slotKey, "free");
                    if (dss.putSwap("cluster." + this.cluster.getId() + ".current.slots", sCurrentSlots, Integer.toString(currentSlots), slotOtherValues)) {
                        break;
                    }
                }
            }

            //*** Slot count has been decremented, we can now delete the actual DSS properties
            dss.deletePrefix(namespacePrefix);
            dss.deletePrefix(slotKey);
        } catch(Exception e) {
            logger.error("Problem discarding the namespace",e);
        }
    }

    private void cleanNamespace() {
        // TODO Auto-generated method stub

    }

    public static void deleteDss(String runName, String clusterId, String namespaceId, IDynamicStatusStoreService dss, IFramework framework) {
        KubernetesClusterImpl cluster = new KubernetesClusterImpl(clusterId, dss, framework);
        KubernetesNamespaceImpl namespace = new KubernetesNamespaceImpl(cluster, namespaceId, framework, dss);
        
        namespace.discard(runName);
    }
    
}
