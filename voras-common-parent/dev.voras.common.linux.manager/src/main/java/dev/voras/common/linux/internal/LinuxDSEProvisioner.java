package dev.voras.common.linux.internal;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.common.linux.LinuxManagerException;
import dev.voras.common.linux.OperatingSystem;
import dev.voras.common.linux.internal.properties.Hostname4;
import dev.voras.common.linux.internal.properties.Hostname6;
import dev.voras.common.linux.spi.ILinuxProvisionedImage;
import dev.voras.common.linux.spi.ILinuxProvisioner;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;

public class LinuxDSEProvisioner implements ILinuxProvisioner {
	
	private final Log logger = LogFactory.getLog(getClass());

	private final LinuxManagerImpl manager;
	private final IConfigurationPropertyStoreService cps;

	public LinuxDSEProvisioner(LinuxManagerImpl manager) {

		this.manager = manager;
		this.cps = this.manager.getCps();
	}

	@Override
	public ILinuxProvisionedImage provision(String tag, OperatingSystem operatingSystem, List<String> capabilities) throws LinuxManagerException {

		try {
			String hostname4 = Hostname4.get(tag);
			String hostname6 = Hostname6.get(tag);
			
			if (hostname4 == null && hostname6 == null) {
				return null;
			}
			
			logger.info("Loaded DSE for Linux Image tagged " + tag);
			
			return new LinuxDSEImage(manager, this.cps, tag, hostname4, hostname6);
		} catch(Exception e) {
			throw new LinuxManagerException("Unable to provision the Linux DSE", e);
		}
	}

}
