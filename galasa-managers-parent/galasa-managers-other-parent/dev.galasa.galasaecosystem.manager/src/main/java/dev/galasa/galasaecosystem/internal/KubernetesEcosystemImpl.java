package dev.galasa.galasaecosystem.internal;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.galasaecosystem.EcosystemEndpoint;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.kubernetes.IConfigMap;
import dev.galasa.kubernetes.IDeployment;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.IPersistentVolumeClaim;
import dev.galasa.kubernetes.IPodLog;
import dev.galasa.kubernetes.IReplicaSet;
import dev.galasa.kubernetes.IResource;
import dev.galasa.kubernetes.IService;
import dev.galasa.kubernetes.IStatefulSet;
import dev.galasa.kubernetes.KubernetesManagerException;

public class KubernetesEcosystemImpl implements IKubernetesEcosystem {

    private final Log                        logger = LogFactory.getLog(getClass());

    private final GalasaEcosystemManagerImpl manager;
    private final IKubernetesNamespace       namespace;
    private final String                     tag;

    private final HashMap<ResourceType, Resource> resources = new HashMap<>();

    private final Yaml                       yaml = new Yaml();

    private final String                     targetVersion = "0.4.0-SNAPSHOT";
    private final HashMap<String, String>    yamlReplacements = new HashMap<>();

    private IHttpClient                      etcdHttpClient;
    private IHttpClient                      apiHttpClient;

    private URL                              cpsUrl;
    private URL                              dssUrl;
    private URL                              rasUrl;
    private URL                              credsUrl;
    private URL                              apiUrl;
    private URI                              cpsUri;
    private URI                              dssUri;
    private URI                              rasUri;
    private URI                              credsUri;

    private URL                              metricsMetricsUrl;
    private URL                              metricsHealthUrl;
    private URL                              resmonMetricsUrl;
    private URL                              resmonHealthUrl;
    private URL                              engineMetricsUrl;
    private URL                              engineHealthUrl;
    private URL                              prometheusUrl;
    private URL                              grafanaUrl;

    public KubernetesEcosystemImpl(GalasaEcosystemManagerImpl manager, String tag, IKubernetesNamespace namespace) {
        this.manager   = manager;
        this.tag       = tag;
        this.namespace = namespace;
    }

    protected void loadYamlResources() throws GalasaEcosystemManagerException {

        ArrayList<Map<String, Object>>   managerYaml = new ArrayList<>();
        ArrayList<Map<String, Object>>   testYaml = new ArrayList<>();

        //*** Setup the blanket replacements
        yamlReplacements.put("${dockerVersion}", targetVersion);

        //*** Load all the yaml files ready for searching and processing
        try {
            IArtifactManager artifactManager = this.manager.getArtifactManager();
            IBundleResources managerBundleResources = artifactManager.getBundleResources(getClass());
            Map<String, InputStream> directoryContents = managerBundleResources.retrieveDirectoryContents("/k8s");

            for(Entry<String, InputStream> entry : directoryContents.entrySet()) {
                String filename = entry.getKey();
                InputStream is = entry.getValue();

                if (filename.endsWith(".yaml")) {
                    loadYaml(yaml, is, managerYaml);
                }

                is.close();
            }
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem loading the YAML for the Kubernetes resources from the manager bundle", e);
        }

        //*** Search for all the resources we need to create the ecosystem
        for(ResourceType type : ResourceType.values()) {
            locateYaml(managerYaml, testYaml, type);
        }            

        logger.debug("All YAML Kubernetes resources loaded");
    }


    private void locateYaml(ArrayList<Map<String, Object>> managerYamls,
            ArrayList<Map<String, Object>> testYamls, 
            ResourceType type) throws GalasaEcosystemManagerException {

        Resource resource = locateYaml(testYamls, type);
        if (resource == null) {
            resource = locateYaml(managerYamls, type);
        }

        if (resource == null) {
            throw new GalasaEcosystemManagerException("Unable to locate YAML for resource " + type.toString());
        }


        this.resources.put(type, resource);
    }

    private Resource locateYaml(ArrayList<Map<String, Object>> yamls, ResourceType type) {
        for(Map<String, Object> yaml : yamls) {
            String kind = getProperty(yaml, "kind");
            String name = getProperty(yaml, "metadata.name");

            if (type.getType().equals(kind) && type.getName().equals(name)) {
                return new Resource(type, yaml);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void loadYaml(Yaml yaml, InputStream is, List<Map<String, Object>> list) throws GalasaEcosystemManagerException {
        try {
            String yamlFile = IOUtils.toString(is, StandardCharsets.UTF_8);
            for(Entry<String, String> entry : yamlReplacements.entrySet()) {
                yamlFile = yamlFile.replace(entry.getKey(), entry.getValue());
            }

            for(Object o : yaml.loadAll(yamlFile) ) {
                if (o instanceof Map) {
                    list.add((Map<String, Object>) o);
                }
            }
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem processing YAML", e);
        }
    }


    public void build() throws GalasaEcosystemManagerException {
        logger.info("Starting the build of Galasa Ecosystem " + this.tag + " Kubernetes namespace " + this.namespace.getFullId());
        Instant buildStart = Instant.now();
        try {
            //*** Build all the external services, need these for some configmaps
            logger.info("Building external services Kubernetes resources");
            build(ResourceType.CPS_EXTERNAL_SERVICE);
            build(ResourceType.RAS_EXTERNAL_SERVICE);
            build(ResourceType.API_EXTERNAL_SERVICE);
            build(ResourceType.METRICS_EXTERNAL_SERVICE);
            build(ResourceType.METRICS_HEALTH_SERVICE);
            build(ResourceType.RESMON_EXTERNAL_SERVICE);
            build(ResourceType.ENGINE_EXTERNAL_SERVICE);
            build(ResourceType.PROMETHEUS_EXTERNAL_SERVICE);
            build(ResourceType.GRAFANA_EXTERNAL_SERVICE);

            //*** Generate the known URLs
            generateKnownUrls();
            modifyBootstrapConfigMap();

            //*** Build all the internal services
            logger.info("Building external services Kubernetes resources");
            build(ResourceType.CPS_INTERNAL_SERVICE);
            build(ResourceType.RAS_INTERNAL_SERVICE);
            build(ResourceType.API_INTERNAL_SERVICE);
            build(ResourceType.METRICS_INTERNAL_SERVICE);
            build(ResourceType.RESMON_INTERNAL_SERVICE);
            build(ResourceType.ENGINE_INTERNAL_SERVICE);
            build(ResourceType.PROMETHEUS_INTERNAL_SERVICE);
            build(ResourceType.GRAFANA_INTERNAL_SERVICE);

            //*** Create all the Persistent Volume Claims
            logger.info("Building Persistent Volume Claim Kubernetes resources");
            build(ResourceType.API_PVC);
            build(ResourceType.PROMETHEUS_PVC);
            build(ResourceType.GRAFANA_PVC);

            //*** Build the config maps.   
            logger.info("Building Config Maps Kubernetes resources");
            build(ResourceType.CONFIG_CONFIGMAP);     
            build(ResourceType.BOOTSTRAP_CONFIGMAP);     
            build(ResourceType.PROMETHEUS_CONFIGMAP);     
            build(ResourceType.GRAFANA_CONFIGMAP);     
            build(ResourceType.GRAFANA_DASHBOARD_CONFIGMAP);     
            build(ResourceType.GRAFANA_AUTODASHBOARD_CONFIGMAP);     
            build(ResourceType.GRAFANA_PROVISIONING_CONFIGMAP);    

            //*** Starting Prometheus and Grafana early as they take a while and doesn't need the CPS
            logger.info("Building Prometheus and Grafana early, as doesn't need the CPS");
            build(ResourceType.PROMETHEUS_DEPLOYMENT);
            build(ResourceType.GRAFANA_DEPLOYMENT);

            //*** Create all the StatefulSets
            logger.info("Building Stateful Set Kubernetes resources");
            build(ResourceType.CPS_STATEFULSET);
            build(ResourceType.RAS_STATEFULSET);

            //*** Wait for the CPS to have completed startup
            logger.info("Waiting for the CPS and RAS to have completed startup");
            waitForMessageInAllPodLogs(ResourceType.CPS_STATEFULSET, "etcd", "serving insecure client requests on [::]:2379", 180);
            waitForMessageInAllPodLogs(ResourceType.RAS_STATEFULSET, "couchdb", "Apache CouchDB has started on http://any:5986/", 180);

            //*** Set the RAS, CREDS properties in the CPS
            logger.info("Setting up initial properties in the CPS");
            storeCpsProperty("framework.dynamicstatus.store", this.dssUri.toString());
            storeCpsProperty("framework.resultarchive.store", this.rasUri.toString());
            storeCpsProperty("framework.credentials.store", this.credsUri.toString());

            //*** Create the API and bootstrap servers
            logger.info("Starting the API server for the bootstrap service");
            build(ResourceType.API_DEPLOYMENT);
            waitForBootstrapPort();

            //*** Start the remaining Deployments
            logger.info("Starting the remaining Deployments");
            build(ResourceType.METRICS_DEPLOYMENT);
            build(ResourceType.RESMON_DEPLOYMENT);
            build(ResourceType.ENGINE_DEPLOYMENT);

            logger.info("Waiting for all the remaining services to start");
            waitForMessageInAllPodLogs(ResourceType.RESMON_DEPLOYMENT, "resource-monitor", "ResourceManagement.run - Resource Manager has started", 180);
            waitForMessageInAllPodLogs(ResourceType.ENGINE_DEPLOYMENT, "engine-controller", "K8sController.run - Kubernetes controller has started", 180);
            waitForMessageInAllPodLogs(ResourceType.METRICS_DEPLOYMENT, "metrics", "MetricsServer.run - Metrics Server has started", 180);
            waitForMessageInAllPodLogs(ResourceType.PROMETHEUS_DEPLOYMENT, "prometheus", "Server is ready to receive web requests", 180);
            waitForMessageInAllPodLogs(ResourceType.GRAFANA_DEPLOYMENT, "grafana", "msg=\"HTTP Server Listen\" logger=http.server address=[::]:3000", 180);
            
            Instant buildEnd = Instant.now();
            long seconds = buildEnd.getEpochSecond() - buildStart.getEpochSecond();

            logger.info("Kubernetes Ecosystem successfully built on " + this.namespace.getFullId() + " in " + seconds + " seconds");

            logger.info("--------------------------------------------------------------------------------------------");
            logger.info("Bootstrap URL = " + this.apiUrl.toString() + "/bootstrap");
            logger.info("CPS URI       = " + this.cpsUri.toString());
            logger.info("DSS URI       = " + this.dssUri.toString());
            logger.info("RAS URI       = " + this.rasUri.toString());
            logger.info("CREDS URI     = " + this.credsUri.toString());
            logger.info("API URL       = " + this.apiUrl.toString());
            logger.info("");
            logger.info("Resource Monitor Metrics URL  = " + this.resmonMetricsUrl);
            logger.info("Resource Monitor Health URL   = " + this.resmonHealthUrl);
            logger.info("Metrics Metrics URL           = " + this.metricsMetricsUrl);
            logger.info("Metrics Health Health URL     = " + this.metricsHealthUrl);
            logger.info("Engine Controller Metrics URL = " + this.engineMetricsUrl);
            logger.info("Engine Controller Health URL  = " + this.engineHealthUrl);
            logger.info("Prometheus URL                = " + this.prometheusUrl);
            logger.info("Grafana URL                   = " + this.grafanaUrl);
            logger.info("--------------------------------------------------------------------------------------------");
        } catch(GalasaEcosystemManagerException e) {
            try {
                this.namespace.saveNamespaceConfiguration();
            } catch (KubernetesManagerException e1) {
                logger.error("Failed to save the Kubernetes namespace configuration for Galasa Ecosystem " + this.tag);
            }
        }
    }

    private void waitForBootstrapPort() throws GalasaEcosystemManagerException {
        logger.debug("Waiting for the bootstrap server to start at " + this.apiUrl.toString() + "/bootstrap");
        try {
            Instant timeout = Instant.now().plusSeconds(180);
            Instant checkMessage = Instant.now().plusSeconds(30);

            IHttpClient client = getApiHttpClient();

            while(timeout.isAfter(Instant.now())) {

                try {
                    HttpClientResponse<String> response = client.getText("/bootstrap");

                    String text = response.getContent();

                    if (text != null && text.contains("framework.config.store=")) {
                        logger.debug("Bootstrap server has started");
                        return;
                    }
                } catch(HttpClientException e) {
                    Throwable t = e.getCause();
                    if (!t.getMessage().contains("Connection refused")) {
                        throw e;
                    }
                }

                if (checkMessage.isBefore(Instant.now())) {
                    logger.debug("Still waiting for bootstrap to start");
                    checkMessage = Instant.now().plusSeconds(30);
                }

                Thread.sleep(2000);
            }

            throw new GalasaEcosystemManagerException("The bootstrap server did not start in time");

        } catch(InterruptedException | KubernetesManagerException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Problem waiting for the bootstrap to become availabe", e);
        }

    }

    private void modifyBootstrapConfigMap() {
        Resource bootstrapResource = this.resources.get(ResourceType.BOOTSTRAP_CONFIGMAP);
        Map<String, Object> yaml = bootstrapResource.getYaml();
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) yaml.get("data");
        String bootstrap = (String) data.get("dev.galasa.bootstrap.cfg");
        bootstrap = bootstrap.replace("${cpsURI}", this.cpsUri.toString());
        data.put("dev.galasa.bootstrap.cfg", bootstrap);  
    }

    private void generateKnownUrls() throws GalasaEcosystemManagerException {
        try {
            this.cpsUrl = getHttpUrl(ResourceType.CPS_EXTERNAL_SERVICE, 2379);
            this.credsUrl = this.cpsUrl;
            this.dssUrl = this.cpsUrl;
            this.cpsUri = new URI("etcd:" + this.cpsUrl.toString());
            this.dssUri = new URI("etcd:" + this.dssUrl.toString());
            this.credsUri = new URI("etcd:" + this.credsUrl.toString());

            this.rasUrl = getHttpUrl(ResourceType.RAS_EXTERNAL_SERVICE, 5984);
            this.rasUri = new URI("couchdb:" + this.rasUrl.toString());

            this.apiUrl = getHttpUrl(ResourceType.API_EXTERNAL_SERVICE, 8181);

            this.metricsMetricsUrl = getHttpUrl(ResourceType.METRICS_EXTERNAL_SERVICE, 9010);
            this.metricsHealthUrl = getHttpUrl(ResourceType.METRICS_HEALTH_SERVICE, 9011);

            this.resmonMetricsUrl = getHttpUrl(ResourceType.RESMON_EXTERNAL_SERVICE, 9010);
            this.resmonHealthUrl = getHttpUrl(ResourceType.RESMON_EXTERNAL_SERVICE, 9011);

            this.engineMetricsUrl = getHttpUrl(ResourceType.ENGINE_EXTERNAL_SERVICE, 9010);
            this.engineHealthUrl = getHttpUrl(ResourceType.ENGINE_EXTERNAL_SERVICE, 9011);

            this.prometheusUrl = getHttpUrl(ResourceType.PROMETHEUS_EXTERNAL_SERVICE, 9090);
            this.grafanaUrl = getHttpUrl(ResourceType.GRAFANA_EXTERNAL_SERVICE, 3000);
        } catch (KubernetesManagerException | MalformedURLException | URISyntaxException e) {
            throw new GalasaEcosystemManagerException("Problem generating the default URLs", e);
        }
    }

    private URL getHttpUrl(ResourceType type, int port) throws KubernetesManagerException, MalformedURLException {
        IService service = (IService) this.resources.get(type).getK8sResource();
        InetSocketAddress socketAddress = service.getSocketAddressForPort(port);
        return new URL("http://" + socketAddress.getHostString() + ":" + Integer.toString(socketAddress.getPort()));
    }

    private void storeCpsProperty(@NotNull String key, @NotNull String value) throws GalasaEcosystemManagerException {
        try {
            Encoder encoder = Base64.getEncoder();
            IHttpClient httpClient = getEtcdHttpClient();

            JsonObject dssJson = new JsonObject();
            dssJson.addProperty("key", new String(encoder.encode(key.getBytes())));
            dssJson.addProperty("value", new String(encoder.encode(value.getBytes())));

            httpClient.postJson("/v3/kv/put", dssJson);

        } catch (KubernetesManagerException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Problem setting CPS property " + key + "=" + value, e);
        }
    }

    private String retrieveCpsProperty(@NotNull String key) throws GalasaEcosystemManagerException {
        try {
            Encoder encoder = Base64.getEncoder();
            Decoder decoder = Base64.getDecoder();
            IHttpClient httpClient = getEtcdHttpClient();

            JsonObject dssJson = new JsonObject();
            dssJson.addProperty("key", new String(encoder.encode(key.getBytes())));

            HttpClientResponse<JsonObject> response = httpClient.postJson("/v3/kv/range", dssJson);
            JsonArray kvs = response.getContent().getAsJsonArray("kvs");
            if (kvs != null && kvs.size() == 1) {
                String encoded = kvs.get(0).getAsJsonObject().get("value").getAsString();
                return new String(decoder.decode(encoded));
            }
            
            return null;
        } catch (KubernetesManagerException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving CPS property " + key, e);
        }
    }

    private void deleteCpsProperty(@NotNull String key) throws GalasaEcosystemManagerException {
        try {
            Encoder encoder = Base64.getEncoder();
            IHttpClient httpClient = getEtcdHttpClient();

            JsonObject dssJson = new JsonObject();
            dssJson.addProperty("key", new String(encoder.encode(key.getBytes())));

            httpClient.postJson("/v3/kv/deleterange", dssJson);
        } catch (KubernetesManagerException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Problem deleting CPS property " + key, e);
        }
    }

    private synchronized IHttpClient getEtcdHttpClient() throws KubernetesManagerException, GalasaEcosystemManagerException {
        if (this.etcdHttpClient != null) {
            return this.etcdHttpClient;
        }

        try {
            this.etcdHttpClient = this.manager.getHttpManager().newHttpClient();
            this.etcdHttpClient.setURI(this.cpsUrl.toURI());
        } catch (URISyntaxException e) {
            throw new KubernetesManagerException("Problem creating the HTTP Client", e);
        }

        return this.etcdHttpClient;
    }

    private synchronized IHttpClient getApiHttpClient() throws KubernetesManagerException, GalasaEcosystemManagerException {
        if (this.apiHttpClient != null) {
            return this.apiHttpClient;
        }

        try {
            this.apiHttpClient = this.manager.getHttpManager().newHttpClient();
            this.apiHttpClient.setURI(this.apiUrl.toURI());
        } catch (URISyntaxException e) {
            throw new KubernetesManagerException("Problem creating the HTTP Client", e);
        }

        return this.apiHttpClient;
    }

    private void waitForMessageInAllPodLogs(ResourceType resourceType, String container, String message, long timeoutInSeconds) throws GalasaEcosystemManagerException {

        try {
            Resource resource = this.resources.get(resourceType);
            logger.debug("Looking for message '" + message + "' in all pods of " + resource.toString() + " in container " + container);

            IResource k8sResource = resource.getK8sResource();
            if (!(k8sResource instanceof IReplicaSet)) {
                throw new GalasaEcosystemManagerException("Tried to access logs on a none ReplicaSet resource - " + resource.toString());
            }

            IReplicaSet podHolder = (IReplicaSet)k8sResource;

            Instant timeout = Instant.now().plusSeconds(timeoutInSeconds);
            Instant checkMessage = Instant.now().plusSeconds(30);

            while(timeout.isAfter(Instant.now())) {
                List<IPodLog> podLogs = podHolder.getPodLogs(container);

                boolean found = true;                
                for(IPodLog podLog : podLogs) {
                    String log = podLog.getLog();

                    if (log == null || !log.contains(message)) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    logger.debug("Found message in all the pods");
                    return;
                }

                if (checkMessage.isBefore(Instant.now())) {
                    logger.debug("Still waiting for message '" + message + "'");
                    checkMessage = Instant.now().plusSeconds(30);
                }

                Thread.sleep(2000);
            }

            throw new GalasaEcosystemManagerException("Did not find message in log within timeout");
        } catch(InterruptedException e) {
            throw new GalasaEcosystemManagerException("Wait for log message interrupted", e);
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem waiting for log message", e);
        }

    }

    private void build(ResourceType resourceType) throws GalasaEcosystemManagerException {
        Resource resource = this.resources.get(resourceType);

        String yamlText = yaml.dump(resource.getYaml());

        try {
            IResource k8sResource = this.namespace.createResource(yamlText);

            resource.setK8sresource(k8sResource);
        } catch (KubernetesManagerException e) {
            throw new GalasaEcosystemManagerException("Unable to create resource " + resourceType.toString() + " in Kubernetes", e);
        }        
    }

    public String getTag() {
        return this.tag;
    }

    public void stop() {
        try {
            this.namespace.saveNamespaceConfiguration();
        } catch (KubernetesManagerException e1) {
            logger.error("Failed to save the Kubernetes namespace configuration for Galasa Ecosystem " + this.tag);
        }
    }

    public void discard() {
       // Do not discard, we will leave this for the Kubernetes Manager to clean up
        logger.debug("Not discarding Galasa Ecosystem " + this.tag + ", leaving for the Kubernetes Manager to do it");
    }

    private static String getProperty(Map<String, Object> yaml, String name) {
        String[] nameParts = name.split("\\.");

        Object currentObject = yaml;
        for(String namePart : nameParts) {
            if (currentObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> currentMap = (Map<String, Object>) currentObject;

                currentObject = currentMap.get(namePart);

                if (currentObject == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        if (currentObject == null) {
            return null;
        }

        if (currentObject instanceof String) {
            return (String)currentObject;
        }

        return null;
    }


    private enum ResourceType {

        CONFIG_CONFIGMAP("ConfigMap", "config", IConfigMap.class),

        CPS_EXTERNAL_SERVICE("Service", "cps-external", IService.class),
        CPS_STATEFULSET("StatefulSet", "cps", IStatefulSet.class),
        CPS_INTERNAL_SERVICE("Service", "cps", IService.class),

        RAS_EXTERNAL_SERVICE("Service", "ras-external", IService.class),
        RAS_INTERNAL_SERVICE("Service", "ras", IService.class),
        RAS_STATEFULSET("StatefulSet", "ras", IStatefulSet.class),

        BOOTSTRAP_CONFIGMAP("ConfigMap", "bootstrap-file", IConfigMap.class),
        API_PVC("PersistentVolumeClaim", "pvc-api", IPersistentVolumeClaim.class),
        API_EXTERNAL_SERVICE("Service", "api-external", IService.class),
        API_INTERNAL_SERVICE("Service", "api", IService.class),
        API_DEPLOYMENT("Deployment","api", IDeployment.class),

        PROMETHEUS_EXTERNAL_SERVICE("Service", "prometheus-external", IService.class),
        PROMETHEUS_INTERNAL_SERVICE("Service", "prometheus", IService.class),
        PROMETHEUS_CONFIGMAP("ConfigMap", "prometheus-config", IConfigMap.class),
        PROMETHEUS_PVC("PersistentVolumeClaim", "pvc-prometheus", IPersistentVolumeClaim.class),
        PROMETHEUS_DEPLOYMENT("Deployment","prometheus", IDeployment.class),

        GRAFANA_EXTERNAL_SERVICE("Service", "grafana-external", IService.class),
        GRAFANA_INTERNAL_SERVICE("Service", "grafana", IService.class),
        GRAFANA_CONFIGMAP("ConfigMap", "grafana-config", IConfigMap.class),
        GRAFANA_PROVISIONING_CONFIGMAP("ConfigMap", "grafana-provisioning", IConfigMap.class),
        GRAFANA_DASHBOARD_CONFIGMAP("ConfigMap", "grafana-dashboard", IConfigMap.class),
        GRAFANA_AUTODASHBOARD_CONFIGMAP("ConfigMap", "grafana-auto-dashboard", IConfigMap.class),
        GRAFANA_PVC("PersistentVolumeClaim", "pvc-grafana", IPersistentVolumeClaim.class),
        GRAFANA_DEPLOYMENT("Deployment","grafana", IDeployment.class),

        METRICS_EXTERNAL_SERVICE("Service", "metrics-external", IService.class),
        METRICS_HEALTH_SERVICE("Service", "metrics-health-external", IService.class),
        METRICS_INTERNAL_SERVICE("Service", "metrics", IService.class),
        METRICS_DEPLOYMENT("Deployment", "metrics", IDeployment.class),

        RESMON_EXTERNAL_SERVICE("Service", "resource-monitor-external", IService.class),
        RESMON_INTERNAL_SERVICE("Service", "resource-monitor", IService.class),
        RESMON_DEPLOYMENT("Deployment", "resource-monitor", IDeployment.class),

        ENGINE_EXTERNAL_SERVICE("Service", "engine-controller-external", IService.class),
        ENGINE_INTERNAL_SERVICE("Service", "engine-controller", IService.class),
        ENGINE_DEPLOYMENT("Deployment", "engine-controller", IDeployment.class);

        private final String type;
        private final String name;

        private ResourceType(String type, String name, Class<? extends IResource> k8sResourceType) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return this.type;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return type + "/" + name;
        }
    }


    private static class Resource {
        private final ResourceType        type;
        private final Map<String, Object> yaml;
        private IResource                 k8sResource;

        private Resource(@NotNull ResourceType type, @NotNull Map<String, Object> yaml) {
            this.type = type;
            this.yaml = yaml;
        }

        public IResource getK8sResource() {
            return this.k8sResource;
        }

        public void setK8sresource(IResource k8sResource) {
            this.k8sResource = k8sResource;
        }

        public @NotNull Map<String, Object> getYaml() {
            return this.yaml;
        }

        @Override
        public String toString() {
            return type.toString();
        }

    }


    @Override
    public @NotNull URI getEndpoint(@NotNull EcosystemEndpoint endpoint) throws GalasaEcosystemManagerException {
        try {
        switch(endpoint) {
            case API:
                return this.apiUrl.toURI();
            case CPS:
                return this.cpsUri;
            case CREDS:
                return this.credsUri;
            case DSS:
                return this.dssUri;
            case ENGINE_CONTROLLER_HEALTH:
                return this.engineHealthUrl.toURI();
            case ENGINE_CONTROLLER_METRICS:
                return this.engineMetricsUrl.toURI();
            case GRAFANA:
                return this.grafanaUrl.toURI();
            case METRICS_HEALTH:
                return this.metricsHealthUrl.toURI();
            case METRICS_METRICS:
                return this.metricsMetricsUrl.toURI();
            case PROMETHEUS:
                return this.prometheusUrl.toURI();
            case RAS:
                return this.rasUri;
            case RESOURCE_MANAGEMENT_HEALTH:
                return this.resmonHealthUrl.toURI();
            case RESOURCE_MANAGEMENT_METRICS:
                return this.resmonMetricsUrl.toURI();
            default:
                throw new GalasaEcosystemManagerException("Unknown Galasa endpoint " + endpoint.toString());
        }
        } catch (URISyntaxException e) {
            throw new GalasaEcosystemManagerException("Problem with endpoint URI", e);
        }
    }

    @Override
    public String getCpsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        if (property == null || property.trim().isEmpty()) {
            throw new GalasaEcosystemManagerException("Property name is missing");
        }
 
        return retrieveCpsProperty(property);
    }

    @Override
    public void setCpsProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        if (property == null || property.trim().isEmpty()) {
            throw new GalasaEcosystemManagerException("Property name is missing");
        }

        if (value == null || value.trim().isEmpty()) {
            deleteCpsProperty(property);
        } else {
            storeCpsProperty(property, value.trim());
        }
    }

    @Override
    public String getDssProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        return getCpsProperty(property);
    }

    @Override
    public void setDssProperty(@NotNull String property, @NotNull String value) throws GalasaEcosystemManagerException {
        setCpsProperty(property, value);
    }

    @Override
    public String getCredsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        return getCpsProperty(property);
    }

    @Override
    public void setCredsProperty(@NotNull String property, @NotNull String value) throws GalasaEcosystemManagerException {
        setCpsProperty(property, value);
    }


}
