package io.ejat.ipnetwork.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IDynamicResource;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.ipnetwork.IIpHost;
import io.ejat.ipnetwork.IIpPort;

public class IpPortImpl implements IIpPort {

	private final IpHostImpl host;
	private final int port;
	private final String type;

	private IpPortImpl(@NotNull IpHostImpl host, int port, @NotNull String type) {
		this.host = host;
		this.port = port;
		this.type = type;
	}

	@Override
	public int getPortNumber() {
		return this.port;
	}

	@Override
	public IIpHost getHost() {
		return this.host;
	}

	@Override
	public String getType() {
		return this.type;
	}
	
	protected static IpPortImpl allocateDss(String runName, IpHostImpl host, int port, String type, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
		String sPort = Integer.toString(port);
		
		String dssPrefix = "host." + host.getHostId() + ".port." + sPort;
		
		//*** First try and assign a control set,  if this fails to swap,  then the port has been allocated by someone else
		HashMap<String, String> otherProps = new HashMap<>();
		otherProps.put("port.run." + runName + "." + dssPrefix, "active");
		otherProps.put(dssPrefix + ".type", type);
		otherProps.put(dssPrefix + ".allocated", Instant.now().toString());
		
		if (!dss.putSwap(dssPrefix, null, runName, otherProps)) {
			return null;
		}
		
		IDynamicResource dssResource = dss.getDynamicResource("host." + host.getHostId() + ".port");
		otherProps = new HashMap<>();
		otherProps.put(sPort, runName);
		otherProps.put(sPort + ".type", type);
		otherProps.put(sPort+ ".allocated", Instant.now().toString());
		dssResource.put(otherProps);
		
		return new IpPortImpl(host, port, type);
	}

	protected static void deleteDss(String runName, String hostId, String port, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
		IDynamicResource dssResource = dss.getDynamicResource("host." + hostId + ".port");
		
		//*** Delete the user view set first before the control set
		HashSet<String> dssProperties = new HashSet<>();
		dssProperties.add(port);
		dssProperties.add(port + ".allocated");
		dssProperties.add(port + ".type");
		dssResource.delete(dssProperties);

		

		//*** Delete the control set
		String dssKey = "host." + hostId + ".port." + port;
		dssProperties = new HashSet<>();
		dssProperties.add(dssKey);
		dssProperties.add(dssKey + ".allocated");
		dssProperties.add(dssKey + ".type");
		dssProperties.add("port.run." + runName + "." + dssKey);
		dss.delete(dssProperties);

	}

	protected void discard(String runName, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
		IpPortImpl.deleteDss(runName, this.host.getHostId(), Integer.toString(port), dss);
	}

}
