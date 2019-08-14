package dev.voras.common.zos.internal;

import javax.validation.constraints.NotNull;

import dev.voras.ICredentials;
import dev.voras.common.ipnetwork.IpNetworkManagerException;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zos.ZosManagerException;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.creds.CredentialsException;
import dev.voras.framework.spi.creds.ICredentialsService;

public abstract class ZosBaseImageImpl implements IZosImage {

	private final ZosManagerImpl zosManager;
	private final IConfigurationPropertyStoreService cps;

	private final String        imageId;
	private final String        clusterId;
	private final String        sysplexID;
	private final String        defaultCredentialsId;
	private final ZosIpHostImpl ipHost;

	private ICredentials defaultCedentials;

	public ZosBaseImageImpl(ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
		this.zosManager = zosManager;
		this.cps = zosManager.getCPS();
		this.imageId    = imageId;
		this.clusterId  = clusterId;

		try {
			this.sysplexID = AbstractManager.nulled(this.cps.getProperty("image." + this.imageId, "sysplex"));
			this.defaultCredentialsId = AbstractManager.defaultString(this.cps.getProperty("image", "credentials", this.imageId), "zos");
		} catch(Exception e) {
			throw new ZosManagerException("Problem populating Image " + this.imageId + " properties", e);
		}

		try {
			this.ipHost = new ZosIpHostImpl(zosManager, imageId);
		} catch(Exception e) {
			throw new ZosManagerException("Unable to create the IP Host for the image " + this.imageId, e);
		}
	}

	protected IConfigurationPropertyStoreService getCPS() {
		return this.cps;
	}

	protected ZosManagerImpl getZosManager() {
		return this.zosManager;
	}

	@Override
	public @NotNull String getImageID() {
		return this.imageId;
	}

	@Override
	public String getSysplexID() {
		return this.sysplexID;
	}

	@Override
	public @NotNull String getClusterID() {
		return this.clusterId;
	}

	@Override
	public @NotNull String getDefaultHostname() throws ZosManagerException {
		return this.ipHost.getHostname();
	}

	@Override
	public ICredentials getDefaultCredentials() throws ZosManagerException {
		if (this.defaultCedentials != null) {
			return this.defaultCedentials;
		}

		try {
			ICredentialsService credsService = zosManager.getFramework().getCredentialsService();

			this.defaultCedentials = credsService.getCredentials(this.defaultCredentialsId);
		} catch (CredentialsException e) {
			throw new ZosManagerException("Unable to acquire the credentials for id " + this.defaultCredentialsId, e);
		}

		if (this.defaultCedentials == null) {
			throw new ZosManagerException("zOS Credentials missing for image " + this.imageId + " id " + this.defaultCredentialsId);
		}

		return defaultCedentials;
	}

	public ZosIpHostImpl getIpHost() {
		return this.ipHost;
	}
	
	@Override
	public String toString() {
		return this.imageId;
	}
}
