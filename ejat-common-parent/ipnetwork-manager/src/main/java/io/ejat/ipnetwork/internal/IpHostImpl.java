package io.ejat.ipnetwork.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IResourcePoolingService;
import io.ejat.framework.spi.InsufficientResourcesAvailableException;
import io.ejat.ipnetwork.IIpPort;
import io.ejat.ipnetwork.IpNetworkManagerException;
import io.ejat.ipnetwork.spi.IIpHostSpi;

public class IpHostImpl implements IIpHostSpi {

	private final static Log logger = LogFactory.getLog(IpHostImpl.class);

	private final IpNetworkManagerImpl ipManager;
	private final IConfigurationPropertyStoreService cps;
	private final IDynamicStatusStoreService dss;
	private final IResourcePoolingService rps;

	private final String hostId;

	private final String defaultHostName;
	private final ArrayList<String> ipv4HostNames = new ArrayList<>();
	private final ArrayList<String> ipv6HostNames = new ArrayList<>();

	private final boolean valid;

	private final ArrayList<IpPortImpl> provisionedPorts = new ArrayList<>();

	public IpHostImpl(IpNetworkManagerImpl ipManager, @NotNull String hostId) throws IpNetworkManagerException {
		this.ipManager = ipManager;
		this.cps    = this.ipManager.getCPS();
		this.dss    = this.ipManager.getDSS();
		this.rps    = this.ipManager.getRPS();
		this.hostId = hostId.toLowerCase();

		try {
			//*** Populate all the fields

			//*** Get the IPV4 host names
			String v4hostnames = IpNetworkManagerImpl.nulled(this.cps.getProperty("host." + this.hostId, "ipv4.hostnames"));
			if (v4hostnames != null) {
				ipv4HostNames.addAll(IpNetworkManagerImpl.split(v4hostnames));
			}

			//*** Get the IPV6 host names
			String v6hostnames = IpNetworkManagerImpl.nulled(this.cps.getProperty("host." + this.hostId, "ipv6.hostnames"));
			if (v6hostnames != null) {
				ipv6HostNames.addAll(IpNetworkManagerImpl.split(v6hostnames));
			}

			//*** Calculate default hostname
			String defHostname = IpNetworkManagerImpl.nulled(this.cps.getProperty("host." + this.hostId, "default.hostname"));
			if (defHostname != null) {
				this.defaultHostName = defHostname;
			} else {
				if (!ipv4HostNames.isEmpty()) {
					this.defaultHostName = ipv4HostNames.get(0);
				} else {
					if (!ipv6HostNames.isEmpty()) {
						this.defaultHostName = ipv6HostNames.get(0);
					} else {
						this.defaultHostName = null;
					}
				}
			}


			//*** Calculate if this host is a valid host
			if (this.defaultHostName != null) {
				this.valid = true;
			} else {
				this.valid = false;
			}
		} catch(Exception e) {
			throw new IpNetworkManagerException("Unable to initialise the IP Host '" + hostId +"'", e);
		}
	}

	@Override
	public String getHostname() {
		return this.defaultHostName;
	}

	@Override
	public boolean isValid() {
		return this.valid;
	}


	@Override
	public IIpPort provisionPort(String type) throws IpNetworkManagerException {
		try {
			String runName = ipManager.getFramework().getTestRunName();

			String availablePorts = IpNetworkManagerImpl.nulled(cps.getProperty("host" , type + ".ports", this.hostId));
			if (availablePorts == null) {
				logger.warn("Defaulting port range for host " + this.hostId + " to 30000-30009");
				availablePorts = "3000{0-9}";
			}

			List<String> availablePortResources = IpNetworkManagerImpl.split(availablePorts);
			List<String> rejectedPorts = new ArrayList<>();

			String dssPrefix = "host." + hostId + ".port.";

			IpPortImpl port = null;
			while(port == null) {
				List<String> possibilities = null;
				try {
					possibilities = rps.obtainResources(availablePortResources, rejectedPorts, 1, dss, dssPrefix);
				} catch(InsufficientResourcesAvailableException e) {
					throw new IpNetworkManagerException("Unable to provision a port as there are no free ports available", e);
				}

				for(String possiblePort : possibilities) {
					port = IpPortImpl.allocateDss(runName, this, Integer.parseInt(possiblePort), type, dss);
					if (port != null) {
						this.provisionedPorts.add(port);
						break;
					}
				}
			}

			logger.info("Provisioned port " + port.getPortNumber() + " on host " + this.hostId + " type " + type);

			return port;
		} catch(Exception e) {
			throw new IpNetworkManagerException("Unable to provision a port for host " + this.hostId + " type=" + type, e);
		}
	}
	
	protected String getHostId() {
		return this.hostId;
	}

	public void provisionDiscard() {
		String runName = this.ipManager.getFramework().getTestRunName();
		for(IpPortImpl port : provisionedPorts) {
			try {
				port.discard(runName, dss);
			} catch (DynamicStatusStoreException e) {
				logger.error("Unable to discard port " + port.getPortNumber() + " on host + " + this.hostId);
			}
		}
	}


}
