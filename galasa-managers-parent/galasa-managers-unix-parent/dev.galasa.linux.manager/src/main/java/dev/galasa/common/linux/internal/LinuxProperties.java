package dev.galasa.common.linux.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.common.linux.LinuxManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

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
