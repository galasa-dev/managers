package dev.galasa.zos.internal;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DssDeletePrefix;
import dev.galasa.framework.spi.DssUpdate;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.properties.PoolPorts;

public class ZosPoolPorts {
	
    private static final Log logger = LogFactory.getLog(ZosPoolPorts.class);

	private final ZosManagerImpl 			 manager;
	private final IDynamicStatusStoreService dss;
	private final IResourcePoolingService 	 rps;	
		
	public ZosPoolPorts(ZosManagerImpl manager, IDynamicStatusStoreService dss, IResourcePoolingService rps) throws ZosManagerException {
		this.manager = manager;
		this.dss = dss;
		this.rps = rps;
	}
	
	public String allocatePort(String image) throws ZosManagerException {

		// The the pool of ports for the specific image
		List<String> resourceStrings = PoolPorts.get(image);
		
		String thePort = null;
		try {
			List<String> zosPorts = this.rps.obtainResources(resourceStrings, null, 1, 1, dss, "zosport");
			
			// Iterate through all the pool ports
			for (String port : zosPorts) {
				try {
					
					// Allocate the ports to the run in the DSS					
					this.dss.performActions(
							new DssUpdate("zosport." + port, this.manager.getFramework().getTestRunName()),
							new DssUpdate("run." + this.manager.getFramework().getTestRunName() + ".zosport." + port, "active"));
					thePort = port;
					
					logger.trace("Allocated z/OS port " + thePort + " from z/OS port pool allocation");
					
				} catch (DynamicStatusStoreException exception) {
					throw new ZosManagerException("Could not update the DSS for port allocation of z/OS port " + port);
				}
			}
		} catch(InsufficientResourcesAvailableException exception)  {
			throw new ZosManagerException("Could not allocate a z/OS port required by the test");
		}
		return thePort;
	}
	
	public static void deleteDss(String port, String run, IDynamicStatusStoreService dss) throws DynamicStatusStoreMatchException, DynamicStatusStoreException {
		dss.performActions(
				new DssDeletePrefix("zosport." + port),
				new DssDeletePrefix("run." + run + ".zosport." + port));
	}
}
