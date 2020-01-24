package dev.galasa.kubernetes.internal;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.properties.KubernetesCredentials;
import dev.galasa.kubernetes.internal.properties.KubernetesMaxSlots;
import dev.galasa.kubernetes.internal.properties.KubernetesNamespaces;
import dev.galasa.kubernetes.internal.properties.KubernetesNodePortProxy;
import dev.galasa.kubernetes.internal.properties.KubernetesUrl;
import dev.galasa.kubernetes.internal.properties.KubernetesValidateCertificate;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.util.Config;

public class KubernetesClusterImpl {

    private final Log                        logger = LogFactory.getLog(getClass());

    private final String                     clusterId;
    private final IDynamicStatusStoreService dss;
    private final IFramework                 framework;
    
    private ApiClient                        apiClient;

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
            newNamespace.initialiseNamespace();
            return newNamespace;
        } catch(InsufficientResourcesAvailableException e) {
            return null;
        } catch(Exception e) {
            logger.warn("Problem allocating namespace",e);
            return null;
        }
    }
    
    public synchronized ApiClient getApi() throws KubernetesManagerException {
        if (this.apiClient != null) {
            return this.apiClient;
        }
        
        URL url = KubernetesUrl.get(this);
        boolean validateCertificate = KubernetesValidateCertificate.get(this);
        String credentialsId = KubernetesCredentials.get(this);
        
        ICredentials credentials = null;
        try {
            ICredentialsService creds = this.framework.getCredentialsService();
            credentials = creds.getCredentials(credentialsId);
        } catch (CredentialsException e) {
            throw new KubernetesManagerException("Problem accessing credentials " + credentialsId, e);
        }
        
        if (credentials == null) {
            throw new KubernetesManagerException("Credentials " + credentialsId + " are missing");
        }
        
        if (!(credentials instanceof ICredentialsToken)) {
            throw new KubernetesManagerException("Credentials " + credentialsId + " is not a token credentials");
        }
        
        
        try {
            this.apiClient = Config.fromToken(url.toString(), new String(((ICredentialsToken)credentials).getToken()), validateCertificate);
            applyNewGson(this.apiClient);
            this.apiClient.setDebugging(false);
            //TODO do, raise issue because Quantity is not being serialized properly
            
            return this.apiClient;
        } catch(Exception e) {
            throw new KubernetesManagerException("Unable the initialise the Kubernetes API Client", e);
        }
        
    }

    private static void applyNewGson(ApiClient apiClient) {
        
        JSON json = apiClient.getJSON();
        Gson gson = json.getGson();
        
        GsonBuilder newGsonBuilder = JSON.createGson();
        newGsonBuilder.registerTypeAdapter(Date.class, gson.getAdapter(Date.class));
        newGsonBuilder.registerTypeAdapter(java.sql.Date.class, gson.getAdapter(java.sql.Date.class));
        newGsonBuilder.registerTypeAdapter(DateTime.class, gson.getAdapter(DateTime.class));
        newGsonBuilder.registerTypeAdapter(LocalDate.class, gson.getAdapter(LocalDate.class));
        newGsonBuilder.registerTypeAdapter(byte[].class, gson.getAdapter(byte[].class));
        newGsonBuilder.registerTypeAdapter(Quantity.class, new Quantity.QuantityAdapter());
        newGsonBuilder.registerTypeAdapter(IntOrString.class, new IntOrString.IntOrStringAdapter());
        Gson newGson = newGsonBuilder.create();
        
        json.setGson(newGson);   
    }

    public String getNodePortProxyHostname() throws KubernetesManagerException {
        return KubernetesNodePortProxy.get(this);
    }
    
    
}
