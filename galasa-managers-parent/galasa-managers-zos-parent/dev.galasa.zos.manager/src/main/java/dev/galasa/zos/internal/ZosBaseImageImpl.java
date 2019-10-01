package dev.galasa.zos.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

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
