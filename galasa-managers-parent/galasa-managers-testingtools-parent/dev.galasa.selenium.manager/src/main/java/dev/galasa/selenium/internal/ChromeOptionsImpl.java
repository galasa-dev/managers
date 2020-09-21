package dev.galasa.selenium.internal;

import java.io.File;
import java.util.List;

import org.openqa.selenium.chrome.ChromeOptions;

import dev.galasa.selenium.IChromeOptions;

/**
 * An Inplementation of ChromeOptions as to avoid test code having dependanicies 
 * on org.openqa.selenium*. 
 */
public class ChromeOptionsImpl implements IChromeOptions {

    public ChromeOptions options;

    public ChromeOptionsImpl() {
        options = new ChromeOptions();
    }

    @Override
    public ChromeOptions getOptions() {
        return this.options;
    }

    @Override
    public void addEncodedExtensions(String... encoded) {
        options.addEncodedExtensions(encoded);
    }

    @Override
    public void addEncodedExtensions(List<String> encoded) {
        options.addEncodedExtensions(encoded);
    }

    @Override
    public void addExtensions(File... paths) {
        options.addExtensions(paths);
    }

    @Override
    public void addExtensions(List<File> paths) {
        options.addExtensions(paths);
    }

    @Override
    public void setAcceptInsecureCerts(boolean bool) {
        options.setAcceptInsecureCerts(bool);
    }

    @Override
    public void setHeadless(boolean bool) {
        options.setHeadless(bool);
    }

    @Override
    public void setBinary(File path) {
        options.setBinary(path);
    }

    @Override
    public void setBinary(String path) {
        options.setBinary(path);
    }

    @Override
    public void addArguments(String... arguments) {
        options.addArguments(arguments);
    }

    @Override
    public void addArguments(List<String> arguments) {
        options.addArguments(arguments);
    }

    @Override
    public void setCapability(String key, Object value) {
        options.setCapability(key, value);
    }

    @Override
    public void setCapability(String key, String value) {
        options.setCapability(key, value);
    }

    @Override
    public void setCapability(String key, Boolean value) {
        options.setCapability(key, value);
    }
    
}