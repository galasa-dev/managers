package dev.galasa.kubernetes.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.properties.KubernetesMaxSlots;
import dev.galasa.kubernetes.internal.properties.KubernetesNamespaces;

public class KubernetesClusterImpl {

    private final Log                        logger = LogFactory.getLog(getClass());

    private final String                     clusterId;
    private final IDynamicStatusStoreService dss;
    private final IFramework                 framework;

    public KubernetesClusterImpl(String clusterId, IDynamicStatusStoreService dss, IFramework framework) {
        this.clusterId = clusterId;     
        this.dss       = dss;
        this.framework = framework;
    }

    public String getId() {
        return this.clusterId;
    }

    /**
     * Return the current availability of namespaces in the cluster
     * 
     * @return a percentage of available namespaces from 0.0-1.0, or null if there is no availability
     * @throws KubernetesManagerException there is problem accessing the CPS or DSS 
     */
    public Float getAvailability() throws KubernetesManagerException {

        try {
            int maxSlots = KubernetesMaxSlots.get(this);
            int currentSlots = 0;
            String sCurrentSlots = dss.get("cluster." + this.clusterId + ".current.slots");
            if (sCurrentSlots != null) {
                currentSlots = Integer.parseInt(sCurrentSlots);
            }

            if (currentSlots >= maxSlots) { 
                return null; // so fuzzy floats don't get involved
            }

            return 1.0f - (((float)currentSlots) / ((float)maxSlots));
        } catch (Exception e) {
            throw new KubernetesManagerException("Unable to determine current slot count for cluster " + this.clusterId, e);
        }
    }

    public KubernetesNamespaceImpl allocateNamespace() {
        try {
            IResourcePoolingService pooling = this.framework.getResourcePoolingService();
            String runName = this.framework.getTestRunName();

            ArrayList<String> rejectedNamespaces = new ArrayList<>();

            String dssKeyPrefix = "cluster." + this.clusterId + ".namespace.";

            List<String> definedNamespaces = KubernetesNamespaces.get(this);

            String selectedNamespace = null;
            while(selectedNamespace == null) {
                // TODO ask pooling to return what it can.   can then set to 10, 1
                List<String> possibleNamespaces = pooling.obtainResources(definedNamespaces, rejectedNamespaces, 1, 1, dss, dssKeyPrefix);

                if (possibleNamespaces.isEmpty()) { // there are no available namespaces
                    return null;
                }

                for(String possibleNamespace : possibleNamespaces) {
                    String namespacePrefix = dssKeyPrefix + possibleNamespace;
                    //*** First reserve the name
                    HashMap<String, String> otherValues = new HashMap<>();
                    otherValues.put(namespacePrefix + ".run", runName);
                    otherValues.put(namespacePrefix + ".allocated", Instant.now().toString());
                    if (!dss.putSwap(namespacePrefix, null, "allocating", otherValues)) {
                        rejectedNamespaces.add(possibleNamespace);
                        continue; //*** Unable to reserve this name,  add to rejected and try next
                    }

                    //*** Now we have a namespace,  increase the slot count
                    while(true) { //*** have to loop around incase another test changed the current slot count
                        int maxSlots = KubernetesMaxSlots.get(this);
                        int currentSlots = 0;
                        String sCurrentSlots = dss.get("cluster." + this.clusterId + ".current.slots");
                        if (sCurrentSlots != null) {
                            currentSlots = Integer.parseInt(sCurrentSlots);
                        }

                        if (currentSlots >= maxSlots) {
                            dss.deletePrefix(namespacePrefix); // Clear the reserved namespace
                            return null; // no availability
                        }

                        currentSlots++;
                        HashMap<String, String> slotOtherValues = new HashMap<>();
                        slotOtherValues.put(namespacePrefix, "active");
                        slotOtherValues.put("slot.run." + runName + ".cluster." + this.clusterId + ".namespace." + possibleNamespace, "active");
                        if (dss.putSwap("cluster." + this.clusterId + ".current.slots", sCurrentSlots, Integer.toString(currentSlots), slotOtherValues)) {
                            selectedNamespace = possibleNamespace;
                            break;
                        }
                    }
                    
                    if (selectedNamespace != null) {
                        break;
                    }
                }
            }
            
            KubernetesNamespaceImpl newNamespace = new KubernetesNamespaceImpl(this, selectedNamespace, this.framework, this.dss);
            return newNamespace;
        } catch(InsufficientResourcesAvailableException e) {
            return null;
        } catch(Exception e) {
            logger.warn("Problem allocating namespace",e);
            return null;
        }
    }

}
