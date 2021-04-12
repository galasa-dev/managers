package dev.galasa.selenium;

import java.util.Random;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.selenium.internal.properties.SeleniumAvailableDrivers;
import dev.galasa.selenium.internal.properties.SeleniumDockerNodeVersion;

public enum Browser {
    FIREFOX,
    OPERA,
    IE, 
    CHROME, 
    EDGE,
    NOTSPECIFIED;
	
	private Browser selected;

    public String getDockerImageName() throws SeleniumManagerException{
        String version = SeleniumDockerNodeVersion.get();
     
        switch (this) {
            case FIREFOX:
                return "selenium/standalone-firefox:"+version;
            case OPERA:
                return "selenium/standalone-opera:"+version;
            case CHROME:
                return "selenium/standalone-chrome:"+version;
            case EDGE:
                return "selenium/standalone-edge:"+version;
            case NOTSPECIFIED:
            	if (selected == null) {
            		selectDriver();
            	}
            	return selected.getDockerImageName();
            	
            default:
                throw new SeleniumManagerException("Unsupported browser. Available docker nodes: Firefox, Chrome, Opera, Edge");
        }
    }
    public String getDriverName() throws SeleniumManagerException {
        switch (this) {
            case FIREFOX:
                return "firefox";
            case OPERA:
                return "opera";
            case CHROME:
                return "chrome";
            case EDGE:
                return "MicrosoftEdge";
            case IE:
                return "internet explorer";
            case NOTSPECIFIED:
            	if (selected == null) {
            		selectDriver();
            	}
            	return selected.getDriverName();
            default:
                throw new SeleniumManagerException("Unsupported driver name.");
        }
    }
    private void selectDriver() throws SeleniumManagerException {
    	try {
    		String[] availabledrivers = SeleniumAvailableDrivers.get();
            if (availabledrivers.length < 1) {
              throw new SeleniumManagerException("No available drivers");
            }
            Random rand = new Random();
            this.selected = Browser.valueOf(availabledrivers[rand.nextInt(availabledrivers.length)]);
    	} catch (ConfigurationPropertyStoreException e) {
    		throw new SeleniumManagerException("Failed to find avilable drivers", e); 
    	}
    }
    
}