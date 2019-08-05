package dev.voras.common.zosmf.internal;

import javax.validation.constraints.NotNull;

import dev.voras.common.zosmf.ZosmfManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IFramework;

public class ZosmfProperties {
	
	private final IConfigurationPropertyStoreService cps;


	public ZosmfProperties(@NotNull IFramework framework) throws ZosmfManagerException {
		try {
			this.cps = framework.getConfigurationPropertyService(ZosmfManagerImpl.NAMESPACE);
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosmfManagerException("Unable to request CPS for the z/OSMF Manager", e);
		}
	}

	public String getZosmfPort(@NotNull String imageId) throws ZosmfManagerException {
		try {
			String zosmfPort = ZosmfManagerImpl.nulled(this.cps.getProperty("zosmf", "port", imageId));
			if (zosmfPort == null) {
				throw new ZosmfManagerException("Value for z/OSMF Port not configured");
			}
			return zosmfPort;
		} catch (Exception e) {
			throw new ZosmfManagerException("Problem asking the CPS for the z/OSMF Port for zOS image "  + imageId, e);
		}
	}
}
