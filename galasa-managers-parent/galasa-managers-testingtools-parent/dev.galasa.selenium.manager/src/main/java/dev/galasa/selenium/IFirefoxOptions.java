/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * A Options pass through interface for the Selenium Options object
 * 
 * @author jamesdavies
 *
 */
public interface IFirefoxOptions {
    public void addPreference(String key, String value);

    public void addPreference(String key, Integer value);

    public void addPreference(String key, Boolean value);

    public FirefoxOptions getOptions();

    public void setProfile(FirefoxProfile profile);

    public void setAcceptInsecureCerts(boolean bool);

    public void setHeadless(boolean bool);

    public void setBinary(Path path);

    public void setBinary(String path);

    public void addArguments(String...arguments);

    public void addArguments(List<String> arguments);

    public void setCapability(String key, Object value);

    public void setLegacy(boolean bool);

    public void setLogLevel(Level level);
}