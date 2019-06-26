package dev.voras.common.linux.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.voras.common.linux.LinuxManagerException;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IFramework;

public class LinuxProperties {
	private final IConfigurationPropertyStoreService cps;

	public LinuxProperties(@NotNull IFramework framework) throws LinuxManagerException {
		try {
			this.cps = framework.getConfigurationPropertyService(LinuxManagerImpl.NAMESPACE);
		} catch (ConfigurationPropertyStoreException e) {
			throw new LinuxManagerException("Unable to request CPS for the Linux Manager", e);
		}
	}

	public List<String> getExtraBundles() throws LinuxManagerException {
		try {
			return AbstractManager.split(this.cps.getProperty("bundle.extra", "managers"));
		} catch (ConfigurationPropertyStoreException e) {
			throw new LinuxManagerException("Problem asking CPS for the extra bundles", e); 
		}
	}


}
