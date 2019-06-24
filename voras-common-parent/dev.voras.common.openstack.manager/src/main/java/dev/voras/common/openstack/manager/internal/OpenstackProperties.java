package dev.voras.common.openstack.manager.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.Constants;
import dev.voras.common.linux.OperatingSystem;
import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IFramework;

public class OpenstackProperties {
	private final static Log logger = LogFactory.getLog(OpenstackProperties.class);
	
	private final IConfigurationPropertyStoreService cps;

	public OpenstackProperties(@NotNull IFramework framework) throws OpenstackManagerException {
		try {
			this.cps = framework.getConfigurationPropertyService(OpenstackManagerImpl.NAMESPACE);
		} catch (ConfigurationPropertyStoreException e) {
			throw new OpenstackManagerException("Unable to request CPS for the Openstack Manager", e);
		}
	}

	@NotNull
	public List<String> getLinuxImages(@NotNull OperatingSystem operatingSystem, String version) throws ConfigurationPropertyStoreException {
		String sImages = null;
		if (version != null) {
			sImages = cps.getProperty("linux", "images", operatingSystem.name());
		} else {
			sImages = cps.getProperty("linux", "images", operatingSystem.name(), version);
		}
		
		return AbstractManager.split(sImages);
	}

	public List<String> getLinuxImageCapabilities(String image) throws ConfigurationPropertyStoreException {
		return AbstractManager.split(cps.getProperty("linux.image." + image, "capabilities"));
	}

	public int getMaxInstances() throws ConfigurationPropertyStoreException {
		String maxInstances = cps.getProperty("server", "maximum");
		
		if (maxInstances == null || maxInstances.trim().isEmpty()) {
			maxInstances = "2";
		}
		
		return Integer.parseInt(maxInstances.trim());
	}

	public List<String> getInstanceNamePool() throws ConfigurationPropertyStoreException {
		List<String> pool = AbstractManager.split(this.cps.getProperty("server", "name.pool"));
		if (pool.isEmpty()) {
			pool.add(Constants.LITERAL_NAME + "{0-9}{0-9}");
		}
		return pool;
	}

	public String getCredentialsId() throws ConfigurationPropertyStoreException {
		return AbstractManager.defaultString(this.cps.getProperty("server", "credentials"), "openstack");
	}

	public String getServerIdentityUri() throws ConfigurationPropertyStoreException {
		return AbstractManager.nulled(this.cps.getProperty("server", "identity"));
	}

	public String getServerIdentityDomain() throws ConfigurationPropertyStoreException {
		return AbstractManager.nulled(this.cps.getProperty("server", "domain"));
	}

	public String getServerIdentityProject() throws ConfigurationPropertyStoreException {
		return AbstractManager.nulled(this.cps.getProperty("server", "project"));
	}

	public int getTimeout() throws ConfigurationPropertyStoreException {
		String sTimeout = AbstractManager.defaultString(this.cps.getProperty("generate", "timeout"), "5");
		try {
			return Integer.parseInt(sTimeout);
		} catch(Exception e) {
			logger.warn("Invalid openstack.generate.timeout, defaulting to 5 minutes",e);
			return 300;
		}
	}

}
