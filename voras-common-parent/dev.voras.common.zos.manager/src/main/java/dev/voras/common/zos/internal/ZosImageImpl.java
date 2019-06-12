package dev.voras.common.zos.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.common.zos.IZosImage;
import dev.voras.common.zos.ZosManagerException;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicResource;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.creds.CredentialsException;
import dev.voras.framework.spi.creds.ICredentials;
import dev.voras.framework.spi.creds.ICredentialsService;
import dev.voras.common.ipnetwork.spi.IIpHostSpi;

public class ZosImageImpl implements IZosImage {

	private final static Log logger = LogFactory.getLog(ZosImageImpl.class);

	private final ZosManagerImpl zosManager;
	private final IConfigurationPropertyStoreService cps;
	private final IDynamicStatusStoreService dss;
	private final IDynamicResource dynamicResource;

	private final String imageId;
	private final String clusterId;
	private final String sysplexID;
	private String defaultCredentialsId;
	private ICredentials defaultCedentials;

	private String allocatedSlotName;
	private IIpHostSpi ipHost;

	public ZosImageImpl(ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
		this.zosManager = zosManager;
		this.dss = zosManager.getDSS();
		this.cps = zosManager.getCPS();
		this.imageId    = imageId;
		this.clusterId  = clusterId;
		this.dynamicResource = this.dss.getDynamicResource("image." + this.imageId);

		try {
			this.sysplexID = AbstractManager.nulled(this.cps.getProperty("image." + this.imageId, "sysplex"));
			this.defaultCredentialsId = AbstractManager.nulled(this.cps.getProperty("image", "credentials", this.imageId));
		} catch(Exception e) {
			throw new ZosManagerException("Problem populating Image " + this.imageId + " properties", e);
		}
	}

	@Override
	public String getImageID() {
		return this.imageId;
	}

	@Override
	public String getSysplexID() {
		return this.sysplexID;
	}

	@Override
	public String getClusterID() {
		return this.clusterId;
	}

	public boolean hasCapacity() throws ZosManagerException {
		if (getCurrentUsage() >= 1.0f) {
			return false;
		}
		return true;
	}

	public Float getCurrentUsage() throws ZosManagerException {
		ZosProperties zosProperties = zosManager.getZosProperties();
		IDynamicStatusStoreService dss = zosManager.getDSS();

		float maxSlots = zosProperties.getImageMaxSlots(this.imageId);
		if (maxSlots <= 0.0f) {
			return 1.0f;
		}

		float usedSlots = 0.0f;
		try {
			String currentSlots = dss.get("image." + this.imageId + ".current.slots");
			if (currentSlots != null) {
				usedSlots = Integer.parseInt(currentSlots);
			}
		} catch (Exception e) {
			throw new ZosManagerException("Problem finding used slots for zOS Image " + imageId, e);
		}

		return usedSlots / maxSlots;
	}

	public boolean allocateImage() throws ZosManagerException {
		ZosProperties zosProperties = zosManager.getZosProperties();
		String runName = zosManager.getFramework().getTestRunName();

		int maxSlots = zosProperties.getImageMaxSlots(this.imageId);
		try {
			int usedSlots = 0;
			String currentSlots = dss.get("image." + this.imageId + ".current.slots");
			if (currentSlots != null) {
				usedSlots = Integer.parseInt(currentSlots);
			}

			if (usedSlots >= maxSlots) {
				return false;
			}

			//*** allocate a slot
			usedSlots++;		
			if (!dss.putSwap("image." + this.imageId + ".current.slots", currentSlots, Integer.toString(usedSlots))) {
				//*** The value of the current slots changed whilst this was running,  so we need to try again with the updated value
				Thread.sleep(200); //*** To avoid race conditions
				return allocateImage();
			}

			//*** Now generate a slot name so that we can track who is using all the slots
			//*** As we dont actually care what the slotname is, to make it relevant add the runname as the slotname
			//*** Need to becareful with immediate reruns of the same run name so generate a unique slotname for each runname instance
			String slotName = "SLOT_" + runName;
			for(int i = 0; ;i++) {
				String actualSlotname = slotName;
				if (i > 0) {
					actualSlotname += "_" + i;
				}

				//*** Try setting the control properties
				String allocated = Instant.now().toString();
				String prefix = "image." + this.imageId + ".slot." + actualSlotname;
				HashMap<String, String> otherProps = new HashMap<>();
				otherProps.put("slot.run." + runName + "." + prefix, "active");
				if (dss.putSwap(prefix, null, runName, otherProps)) {
					allocatedSlotName = actualSlotname;

					String resPrefix = "slot." + this.allocatedSlotName;
					//*** Set the user view properties
					HashMap<String, String> resProps = new HashMap<>();
					resProps.put(resPrefix, runName);
					resProps.put(resPrefix + ".allocated", allocated);
					dynamicResource.put(resProps);
					break;
				}
			}

			//*** Obtain a IpHost for the image
			String hostId = zosManager.getZosProperties().getHostId(this);
			this.ipHost = zosManager.getIpManager().buildHost(hostId);
		} catch (Exception e) {
			throw new ZosManagerException("Problem finding used slots for zOS Image " + imageId, e);
		}

		return true;
	}

	public String getSlotName() {
		return this.allocatedSlotName;
	}

	public void freeImage() {
		try {
			String currentSlots = dss.get("image." + this.imageId + ".current.slots");

			if (currentSlots == null) {
				return; // Missing value, no need to update
			}

			int usedSlots = Integer.parseInt(currentSlots);
			usedSlots--;
			if (usedSlots < 0) {
				usedSlots = 0;
			}

			String runName = zosManager.getFramework().getTestRunName();

			//*** Remove the userview set
			String resPrefix = "slot." + this.allocatedSlotName;
			//*** delete the user view properties
			HashSet<String> resProps = new HashSet<>();
			resProps.add(resPrefix);
			resProps.add(resPrefix + ".allocated");
			dynamicResource.delete(resProps);

			//*** Remove the control set
			String prefix = "image." + this.imageId + ".slot." + this.allocatedSlotName;
			HashMap<String, String> otherProps = new HashMap<>();
			otherProps.put("slot.run." + zosManager.getFramework().getTestRunName() + "." + prefix, "free");
			if (!dss.putSwap("image." + this.imageId + ".current.slots", currentSlots, Integer.toString(usedSlots), otherProps)) {
				//*** The value of the current slots changed whilst this was running,  so we need to try again with the updated value
				Thread.sleep(200); //*** To avoid race conditions
				freeImage();
				return;
			}

			HashSet<String> delProps = new HashSet<>();
			delProps.add("slot.run." + runName + "." + prefix);
			delProps.add(prefix);
			dss.delete(delProps);

			dss.delete("image." + this.imageId + ".current.slot." + this.allocatedSlotName);

			logger.info("Discard slot name " + this.allocatedSlotName + " for zOS Image " + this.imageId);
		} catch (Exception e) {
			logger.warn("Failed to free slot on image " + this.imageId + ", slot " + this.allocatedSlotName + ", leaving for manager clean up routines", e);
		}
	}

	@NotNull
	protected IIpHostSpi getIpHost() {
		return this.ipHost;
	}

	public static void deleteDss(String runName, String imageId, String slot, IDynamicStatusStoreService dss) {
		//*** Have to be careful with the delete as we need to keep the current.slots count correct

		try {
			//*** First clear the User view properties
			IDynamicResource dynamicResource = dss.getDynamicResource("image." + imageId);

			String resPrefix = "slot." + slot;

			HashSet<String> resProps = new HashSet<>();
			resProps.add(resPrefix);
			resProps.add(resPrefix + ".allocated");
			dynamicResource.delete(resProps);

			//*** Now get the run slot to see if it is still active,   if so,  try to switch to free
			String prefix = "image." + imageId + ".slot." + slot;
			String runSlot = dss.get("slot.run." + runName + "." + prefix);
			if ("active".equals(runSlot)) {  //*** The slot is still active so try and free it
				if (dss.putSwap("slot.run." + runName + "." + prefix, "active", "free")) {
					//*** Managed it,   decrement the current slots by 1,  may need a couple attempts as someone else may be updating
					while(true) {
						String sCurrentSlots = dss.get("image." + imageId + ".current.slots");
						int currentSlots = Integer.parseInt(sCurrentSlots);
						currentSlots--;
						if (currentSlots < 0) {
							currentSlots = 0;
						}

						if (dss.putSwap("image." + imageId + ".current.slots", sCurrentSlots, Integer.toString(currentSlots))) {
							break;
						}

						Thread.sleep(100);
					}
				}
			}

			//*** Delete the slot records
			HashSet<String> props = new HashSet<>();
			props.add(prefix);
			props.add("slot.run." + runName + "." + prefix);
			dss.delete(props);
		} catch(Exception e) {
			logger.error("Failed to discard slot " + slot + " on image " + imageId,e);
		}

	}
	
	
	@Override
	public ICredentials getDefaultCredentials() throws ZosManagerException {
		if (this.defaultCedentials != null) {
			return this.defaultCedentials;
		}
		
		if (this.defaultCredentialsId == null) {
			this.defaultCredentialsId = "zos";
			logger.warn("Credentials ID not set for zOS Image " + this.imageId + ", defaulting to 'zos'");
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

}
