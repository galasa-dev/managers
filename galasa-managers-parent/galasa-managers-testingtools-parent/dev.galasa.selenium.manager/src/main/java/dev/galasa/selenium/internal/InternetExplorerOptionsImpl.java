package dev.galasa.selenium.internal;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.ie.InternetExplorerOptions;

import dev.galasa.selenium.IInternetExplorerOptions;

/**
 * An Inplementation of InternetExplorerOptions as to avoid test code having dependanicies 
 * on org.openqa.selenium*. 
 */
public class InternetExplorerOptionsImpl implements IInternetExplorerOptions {

    public InternetExplorerOptions options;

    public InternetExplorerOptionsImpl() {
        options = new InternetExplorerOptions();
    }

    @Override
    public InternetExplorerOptions getOptions() {
        return this.options;
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

    @Override
    public void destructivelyEnsureCleanSession() {
        options.destructivelyEnsureCleanSession();
    }

    @Override
    public void disableNativeEvents() {
        options.disableNativeEvents();
    }

    @Override
    public void enablePersistentHovering() {
        options.enablePersistentHovering();
    }

    @Override
    public void ignoreZoomSettings() {
        options.ignoreZoomSettings();
    }

    @Override
    public void introduceFlakinessByIgnoringSecurityDomains() {
        options.introduceFlakinessByIgnoringSecurityDomains();
    }

    @Override
    public void requireWindowFocus() {
        options.requireWindowFocus();
    }

    @Override
    public void useCreateProcessApiToLaunchIe() {
        options.useCreateProcessApiToLaunchIe();
    }

    @Override
    public void waitForUploadDialogUpTo(long duration, TimeUnit unit) {
        options.waitForUploadDialogUpTo(duration, unit)    ;
    }

    @Override
    public void withAttachTimeout(long duration, TimeUnit unit) {
        options.withAttachTimeout(duration, unit);
    }

    @Override
    public void withInitialBrowserUrl(String url) {
        options.withInitialBrowserUrl(url);
    }

    @Override
    public void addCommandSwitches(String... switches) {
        options.addCommandSwitches(switches);
    }
    
}