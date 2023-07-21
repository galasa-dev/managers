/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.resourcemanagement;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;

@Component(service= {IResourceManagementProvider.class})
public class SemResourceManagement implements IResourceManagementProvider {

    private IFramework                         framework;
    private IResourceManagement                resourceManagement;
    private IDynamicStatusStoreService         dss;
    private IConfigurationPropertyStoreService cps;

    private ApplidResourceMonitor applidResourceMonitor;
    private PortResourceMonitor   portResourceMonitor;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {

        this.framework = framework;
        this.resourceManagement = resourceManagement;
        try {
            this.dss = this.framework.getDynamicStatusStoreService("sem");
            this.cps = this.framework.getConfigurationPropertyService("sem");
        } catch (Exception e) {
            throw new ResourceManagerException("Unable to initialise SEM resource monitor", e);
        }

        this.applidResourceMonitor = new ApplidResourceMonitor(framework, resourceManagement, this.dss, this, cps);
        this.portResourceMonitor = new PortResourceMonitor(framework, resourceManagement, this.dss, this, cps);

        return true;
    }

    @Override
    public void start() {
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.applidResourceMonitor, 
                this.framework.getRandom().nextInt(20),
                60, 
                TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.portResourceMonitor, 
                this.framework.getRandom().nextInt(60),
                60, 
                TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
        this.applidResourceMonitor.runFinishedOrDeleted(runName);
        this.portResourceMonitor.runFinishedOrDeleted(runName);
    }

}
