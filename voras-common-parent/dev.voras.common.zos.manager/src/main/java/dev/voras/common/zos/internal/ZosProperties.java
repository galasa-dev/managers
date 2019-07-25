package dev.voras.common.zos.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.voras.common.zos.ZosManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IFramework;

public class ZosProperties {

	private final IConfigurationPropertyStoreService cps;

	public ZosProperties(@NotNull IFramework framework) throws ZosManagerException {
		try {
			this.cps = framework.getConfigurationPropertyService(ZosManagerImpl.NAMESPACE);
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Unable to request CPS for the zOS Manager", e);
		}
	}

	public String getBatchExtraBundle() throws ZosManagerException {
		try {
			String batchBundleName = ZosManagerImpl.nulled(this.cps.getProperty("bundle.extra", "batch.manager"));
			if (batchBundleName == null)  {
				return "dev.voras.common.zosbatch.zosmf.manager";
			}
			return batchBundleName;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking CPS for the batch manager extra bundle name", e); 
		}
	}

	public String getImageIdForTag(String tag) throws ZosManagerException {
		try {
			return ZosManagerImpl.nulled(this.cps.getProperty("tag", "imageid", tag));
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking the CPS for the image id for tag '"  + tag + "'", e); 
		}
	}

	public String getDseImageIdForTag(String tag) throws ZosManagerException {
		try {
			return ZosManagerImpl.nulled(this.cps.getProperty("dse.tag", "imageid", tag));
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking the CPS for the image DSE id for tag '"  + tag + "'", e); 
		}
	}

	public String getClusterIdForTag(String tag) throws ZosManagerException {
		try {
			return ZosManagerImpl.nulled(this.cps.getProperty("tag", "clusterid", tag));
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking the CPS for the cluster id for tag '"  + tag + "'", e); 
		}
	}

	@NotNull
	public List<String> getClusterImages(String clusterId) throws ZosManagerException {
		try {
			List<String> images = ZosManagerImpl.split(this.cps.getProperty("cluster", clusterId + ".images"));
			if (images.isEmpty()) {
				throw new ZosManagerException("Unable to locate zOS images for cluster " + clusterId + ", see property zos.cluster.*.images");
			}
			return images;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking the CPS for the cluster images for cluster '"  + clusterId + "'", e); 
		}
	}

	public int getImageMaxSlots(String imageId) throws ZosManagerException {
		try {
			String slots = ZosManagerImpl.nulled(this.cps.getProperty("image", "max.slots", imageId));
			if (slots == null) {
				return 2;
			}
			return Integer.parseInt(slots);
		} catch (Exception e) {
			throw new ZosManagerException("Problem asking the CPS for the zOS image "  + imageId+ " max slots", e); 
		}
	}


	/**
	 * Retrieves the IP Host ID of the zOS Image.   
	 * 
	 *  If CPS property zos.image.xxxxx.iphostid exists,  then that is returned, otherwise the imageid is returned
	 * 
	 * @param image the zosImage
	 * @return a IP Host ID for use with the IP Network Manager
	 * @throws ZosManagerException if there are issues with the CPS
	 */
	@NotNull
	public String getHostId(ZosProvisionedImageImpl image) throws ZosManagerException {
		String imageId = image.getImageID();
		try {
			String hostid = ZosManagerImpl.nulled(this.cps.getProperty("image." + image.getImageID(), "iphostid"));
			if (hostid == null) {
				return imageId.toLowerCase();
			}
			return hostid.toLowerCase();
		} catch (Exception e) {
			throw new ZosManagerException("Problem asking the CPS for the zOS image "  + imageId + " ip host id", e); 
		}
	}
}
