/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.selenium;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

import org.openqa.selenium.chrome.ChromeOptions;

/**
 * A Options pass through interface for the Selenium Options object
 * 
 * @author jamesdavies
 *
 */
public interface IChromeOptions {

    public ChromeOptions getOptions();

    public void addEncodedExtensions(String... encoded);

    public void addEncodedExtensions(List<String> encoded);

    public void addExtensions(File... paths);

    public void addExtensions(List<File> paths);

    public void setAcceptInsecureCerts(boolean bool);

    public void setHeadless(boolean bool);

    public void setBinary(File path);

    public void setBinary(String path);

    public void addArguments(String...arguments);

    public void addArguments(List<String> arguments);

    public void setCapability(String key, Object value);

    public void setCapability(String key, String value);

    public void setCapability(String key, Boolean value);
    
}