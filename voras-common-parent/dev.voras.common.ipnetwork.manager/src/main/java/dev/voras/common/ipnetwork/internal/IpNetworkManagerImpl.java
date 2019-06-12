package dev.voras.common.ipnetwork.internal;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.voras.ManagerException;
import dev.voras.common.ipnetwork.IpNetworkManagerException;
import dev.voras.common.ipnetwork.spi.IIpHostSpi;
import dev.voras.common.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IManager;
import dev.voras.framework.spi.IResourcePoolingService;

@Component(service = { IManager.class })
public class IpNetworkManagerImpl extends AbstractManager implements IIpNetworkManagerSpi {
	protected final static String NAMESPACE = "ipnetwork";

	@SuppressWarnings("unused")
	private final static Log logger = LogFactory.getLog(IpNetworkManagerImpl.class);

	private IFramework framework;
	private IConfigurationPropertyStoreService cps;
	private IDynamicStatusStoreService dss;

	private final HashMap<String, IpHostImpl> hosts = new HashMap<>();

	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);

		try {
			this.framework = framework;
			this.cps = framework.getConfigurationPropertyService(NAMESPACE);
			this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
		} catch(Exception e) {
			throw new IpNetworkManagerException("Unable to initialise the IP Network Manager", e);
		}
	}

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
	}

	@Override
	public @NotNull IIpHostSpi buildHost(String hostId) throws IpNetworkManagerException {
		if (this.hosts.containsKey(hostId)) {
			return this.hosts.get(hostId);
		}

		IpHostImpl newHost = new IpHostImpl(this, hostId);
		this.hosts.put(hostId, newHost);

		return newHost;
	}
	
	@Override
	public void provisionDiscard() {
		
		//*** discard anything the hosts created
		for(IpHostImpl host : hosts.values()) {
			host.provisionDiscard();
		}
		
		super.provisionDiscard();
	}

	public IConfigurationPropertyStoreService getCPS() {
		return this.cps;
	}

	public IDynamicStatusStoreService getDSS() {
		return this.dss;
	}

	public IResourcePoolingService getRPS() {
		return this.framework.getResourcePoolingService();
	}

}
