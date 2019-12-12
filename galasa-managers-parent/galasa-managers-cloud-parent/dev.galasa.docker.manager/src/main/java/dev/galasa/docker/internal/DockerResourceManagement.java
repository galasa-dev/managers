package dev.galasa.docker.internal;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;

/**
 * Resource management for the docker slots used to run docker containers
 * 
 * @author Jmes Davies
 */
@Component(service = { IResourceManagementProvider.class })
public class DockerResourceManagement implements IResourceManagementProvider {

    private IFramework                              framework;
    private IResourceManagement                     resourceManagement;
    private IDynamicStatusStoreService              dss;
    private IConfigurationPropertyStoreService      cps;

    private DockerResourceMonitor                   slotResourceMonitor;

    /**
     * Initialses the resource management of the docker slots.
     * 
     * @param framework
     * @param resourceManagement
     * @throws ResourceManagement
     */
    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.framework              = framework;
        this.resourceManagement     = resourceManagement;

        try {
            this.cps = framework.getConfigurationPropertyService("docker");
            this.dss = framework.getDynamicStatusStoreService("docker");
        } catch(DynamicStatusStoreException e) {
            throw new ResourceManagerException("Could not initialise docker resource monitor, due to the CPS:  ", e);
        } catch(ConfigurationPropertyStoreException e) {
            throw new ResourceManagerException("Could not initialise docker resource monitor, due to the DSS:  ", e);
        }
        slotResourceMonitor = new DockerResourceMonitor(framework, resourceManagement, dss, this, cps);
        return true;

    }

    /**
     * Start the resource management of docker slots
     */
    @Override
    public void start() {
       this.resourceManagement.
       getScheduledExecutorService().
       scheduleWithFixedDelay(slotResourceMonitor, this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
    }

    /**
     * Shutdown resource management of docker slots.
     */
    @Override
    public void shutdown() {

    }

    /**
     * Run finished or deleted for a specified runName
     * 
     * @param runName
     */
    @Override
    public void runFinishedOrDeleted(String runName) {
        this.slotResourceMonitor.runFinishedOrDeleted(runName);
    }
    
}