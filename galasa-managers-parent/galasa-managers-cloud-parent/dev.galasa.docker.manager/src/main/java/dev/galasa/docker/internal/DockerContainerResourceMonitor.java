package dev.galasa.docker.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.StandAloneHttpClient;

/**
 * Resource monitor for cleaning up orphaned containers.
 * 
 * @author James Davies
 */
public class DockerContainerResourceMonitor implements Runnable {
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IConfigurationPropertyStoreService cps;
    private final IDynamicStatusStoreService dss;

    private final Log logger = LogFactory.getLog(DockerContainerResourceMonitor.class);

    private Gson gson = new Gson();

    private Map<String,IHttpClient> dockerEngines = new HashMap<>();

    public DockerContainerResourceMonitor(IFramework framework, IResourceManagement resourceManagement, IConfigurationPropertyStoreService cps, IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.cps = cps;
        this.dss = dss;

        dockerEngines = getDockerEngines();

        logger.info("Docker container resource monitor intialised");
    }

    @Override
    public void run() {
        logger.info("Docker container resouce check started");

        for (String engine : dockerEngines.keySet()) {
            List<String> containers = getOrphanedContainers(engine, dockerEngines.get(engine));
            logger.info("Engine " + engine + " has " + containers.size() + " orphaned containers found");
            if (containers.size() > 0) {
                killContainers(containers, dockerEngines.get(engine));
            }
        }
        logger.info("Docker container resouce check Finished");
    }

    /**
     * Stops and removed containers from an engines
     * @param containers
     * @param client
     */
    private void killContainers(List<String> containers, IHttpClient client) {
        logger.info("Shutting down oprhaned containers");
        try {
            for (String id : containers) {
                HttpClientResponse<String> resp = client.deleteText("/containers/"+id+"?force=true");
                if (resp.getStatusCode() != 204) {
                    logger.error("Something went wrong when removing container: " + resp.getStatusLine());
                }
            }
        } catch (HttpClientException e) {
            logger.error("Failed to kill containers.", e);
        }        
    }

    /**
     * Looks at all containers on a Engine, locates Galasa specifics and ensures they have a decicated slot
     * @param engine
     * @param client
     * @return
     */
    private List<String> getOrphanedContainers(String engine, IHttpClient client) {
        List<String> orphanedContainers = new ArrayList<>();
        try {
            HttpClientResponse<String> resp = client.getText("/containers/json");
            if (resp.getStatusCode() != 200) {
                logger.error("Something went wrong when retrieving containers: " + resp.getStatusLine());
            }

            DockerContainerJSON[] activeContainers = gson.fromJson(resp.getContent(), DockerContainerJSON[].class);
            for (DockerContainerJSON container : activeContainers) {
                String runName = parseRunId(container.getNames());
                // Other non Galasa pod
                if (runName == null) {
                    continue;
                }

                Collection<String> activeRunNames = dss.getPrefix("engine."+engine+".slot").values();
                if (!activeRunNames.contains(runName)) {
                    orphanedContainers.add(container.getId());
                }
            }
        } catch (HttpClientException | FrameworkException e) {
            logger.error("Failed to get containers.", e);
        }
        return orphanedContainers;
    }

    /**
     * Looks at CPS for the docker Engines available
     * @return
     */
    private Map<String, IHttpClient> getDockerEngines() {
        HashMap<String, IHttpClient> engines = new HashMap<>();
        try {
            String[] enginesTags = cps.getPrefixedProperties("engines").get("engines").split(",");
            for (String engine : enginesTags) {
                String hostname = cps.getProperty("engine", "hostname", engine);
                String port = cps.getProperty("engine", "port", engine);

                IHttpClient client = StandAloneHttpClient.getHttpClient(3600, logger);
                client.setURI(new URI(hostname+":"+port));

                engines.put(engine, client);
            }
        } catch (ConfigurationPropertyStoreException | URISyntaxException e) {
            logger.error("Failed to get docker engines.", e);
        }
        return engines;
    }

    /**
     * Locates the Run ID from the container name.
     * 
     * Name is always structure like "GASLAS_RUNID_CONTAINERTAG"
     * @param names
     * @return
     */
    private String parseRunId(String[] names) {
        for (String name: names) {
            if (name.contains("GALASA_")) {
                String[] containerComponents = name.split("_");
                logger.info("Image found for Run: " + containerComponents[1] + " tagged with: " + containerComponents[2]);
                return containerComponents[1];
            }
        }
        return null;
    }
    
}