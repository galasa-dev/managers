package io.ejat.zos.internal;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IResourceManagement;
import io.ejat.framework.spi.IResourceManagementProvider;
import io.ejat.framework.spi.ResourceManagerException;

@Component(service= {IResourceManagementProvider.class})
public class ZosResourceManagement implements IResourceManagementProvider {

	private IFramework                         framework;
	private IResourceManagement                resourceManagement;
	private IDynamicStatusStoreService         dss;
	private IConfigurationPropertyStoreService cps;
	
	private SlotResourceMonitor                slotResourceMonitor;
	
	@Override
	public boolean initialise(IFramework framework, IResourceManagement resourceManagement) throws ResourceManagerException {
		this.framework = framework;
		this.resourceManagement = resourceManagement;
		try {
			this.dss = this.framework.getDynamicStatusStoreService("zos");
			this.cps = this.framework.getConfigurationPropertyService("zos");
		} catch (Exception e) {
			throw new ResourceManagerException("Unable to initialise IP Network resource monitor", e);
		}
		
		// TODO Must add a check every 5 minutes to tidy up all the properties that may have been left hanging
		
		slotResourceMonitor = new SlotResourceMonitor(framework, resourceManagement, dss, this, cps);
		
		return true;
	}

	@Override
	public void start() {
		this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(slotResourceMonitor, 
				this.framework.getRandom().nextInt(20),
				20, 
				TimeUnit.SECONDS);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void runFinishedOrDeleted(String runName) {
		this.slotResourceMonitor.runFinishedOrDeleted(runName);
	}

}
