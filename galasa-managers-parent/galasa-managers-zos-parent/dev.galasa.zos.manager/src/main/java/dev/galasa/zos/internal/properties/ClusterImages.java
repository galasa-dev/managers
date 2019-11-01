/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * The images for a zOS Cluster
 * <p>
 * The zOS Images for the specifies Cluster
 * </p><p>
 * The property is:<br>
 * {@code zos.cluster.[clusterId].images=SYSA,SYSB,SYSC} 
 * </p>
 * <p>
 * There is no default
 * </p>
 *
 */
public class ClusterImages extends CpsProperties {
	
	public static List<String> get(String clusterId) throws ZosManagerException {
		try {
			List<String> images = getStringList(ZosPropertiesSingleton.cps(), "cluster", clusterId + ".images");
			if (images.isEmpty()) {
				throw new ZosManagerException("Unable to locate zOS images for cluster " + clusterId + ", see property zos.cluster.*.images");
			}
			return images;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking the CPS for the cluster images for cluster '"  + clusterId + "'", e);
		}
	}

}
