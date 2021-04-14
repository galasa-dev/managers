package dev.galasa.selenium.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssDeletePrefix;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.internal.properties.SeleniumDriverMaxSlots;
import dev.galasa.selenium.internal.properties.SeleniumWebDriverType;

public class SeleniumEnvironment {
	
	private SeleniumManagerImpl seleniumManager;
	private Path screenshotRasDirectory;
		
	private List<String> slots = new ArrayList<>();
	private List<ISeleniumManager> drivers = new ArrayList<>();
	
	private IDynamicStatusStoreService dss;
	private String runName;
	
	public SeleniumEnvironment(SeleniumManagerImpl seleleniumManager, Path screenshotRasDirectory) throws SeleniumManagerException {
		this.seleniumManager = seleleniumManager;
		this.screenshotRasDirectory = screenshotRasDirectory;
		this.dss = seleleniumManager.getDss();
		this.runName = seleniumManager.getFramework().getTestRunName();
	}
	
	public ISeleniumManager allocateDriver(Browser browser) throws SeleniumManagerException {
		ISeleniumManager driver;
		Path driverRasDir = screenshotRasDirectory.resolve("driver_"+drivers.size());
		// Get a slot or fail
		String slotName = allocateSlot();
		
		try {
			switch(SeleniumWebDriverType.get()) {
		    case ("local"):
		    	driver = new LocalDriverImpl(browser, driverRasDir);
		    	break;
		    default:
		    	driver =  new RemoteDriverImpl(this, seleniumManager, browser, slotName, driverRasDir);
		    	break;
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Failed to fetch Driver type", e);
		}
		drivers.add(driver);
		return driver;

	}
	
	private String allocateSlot() throws SeleniumManagerException {
		String slotKey = "driver.current.slots";
		String slots = "";
		String slotName = "";
		int currentSlots = 0;
		try {
			slots = dss.get(slotKey);
			if (slots != null) {
				currentSlots = Integer.valueOf(slots);
			}
			
			if (currentSlots >= SeleniumDriverMaxSlots.get()) {
				throw new SeleniumManagerException("Failed to provsion. No slots avilable");
			}			
			
			String slotNamePrefix = "SeleniumSlot_" + this.runName + "_";
			
			int counter = 0;
			while ("".equals(slotName)) {
				String slotNameAttempt = slotNamePrefix + counter;
				if (dss.get("driver.slot." + slotNameAttempt)==null) {
					// Found a slot name;
					slotName = slotNameAttempt;
					break;
				}
				
				counter++;
			}
			
		} catch (DynamicStatusStoreException | ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Failed ot allocate slot", e);
		}
		
		try {
			dss.performActions(
					new DssSwap(slotKey, slots, String.valueOf(currentSlots+1)),
					new DssAdd("driver.slot." + slotName, runName)
					);
			this.slots.add(slotName);
		} catch (DynamicStatusStoreException e) {
			// Failed to set with race conditions, try again
			allocateSlot();
		}
		return slotName;
	}

	
	public void screenShotPages() {
//		for (ISeleniumManager driver: drivers) {
//			driver.
//		}
	}

	public void discard() throws SeleniumManagerException {
		for (ISeleniumManager driver: drivers) {
			driver.discard();
		}
		freeSlots();
	}
	
	private void freeSlots() throws SeleniumManagerException {
		List<IDssAction> actions = new ArrayList<>();
		try {
			for (String slot: slots) {
				actions.add(new DssDeletePrefix("driver.slot."+slot));
			}
			String currentSlots = dss.get("driver.current.slots");
			int newSlots = Integer.valueOf(currentSlots)-1;
			
			actions.add(new DssSwap("driver.current.slots", currentSlots, String.valueOf(newSlots)));
			
			dss.performActions(actions.toArray(new IDssAction[actions.size()]));
		} catch (DynamicStatusStoreException e) {
			throw new SeleniumManagerException("Failed to clean slots", e);
		}
	}
}
