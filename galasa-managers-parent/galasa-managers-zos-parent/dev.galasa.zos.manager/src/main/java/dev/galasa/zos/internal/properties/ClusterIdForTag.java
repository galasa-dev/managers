package dev.galasa.zos.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * The zOS Cluster ID
 * <p>
 * The Cluster ID for the specified tag 
 * </p><p>
 * The property is:<br>
 * {@code zos.tag.[tag].clusterid=plex1} 
 * </p>
 * <p>
 * There is no default
 * </p>
 *
 */
public class ClusterIdForTag extends CpsProperties {
	
	public static String get(@NotNull String tag) throws ZosManagerException {
		try {
			return getStringNulled(ZosPropertiesSingleton.cps(), "tag", "clusterid", tag);
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking the CPS for the cluster id for tag '"  + tag + "'", e);
		}
	}
}
