package dev.galasa.selenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.ie.InternetExplorerOptions;

public interface IInternetExplorerOptions {

    public InternetExplorerOptions getOptions();

    public void destructivelyEnsureCleanSession();

    public void disableNativeEvents();

    public void enablePersistentHovering();

    public void ignoreZoomSettings();

    public void introduceFlakinessByIgnoringSecurityDomains();

    public void requireWindowFocus();

    public void useCreateProcessApiToLaunchIe();

    public void waitForUploadDialogUpTo(long duration, TimeUnit unit);

    public void withAttachTimeout(long duration, TimeUnit unit);

    public void withInitialBrowserUrl(String url);

    public void addCommandSwitches(String... switches);

    public void setCapability(String key, Object value);

    public void setCapability(String key, String value);

    public void setCapability(String key, Boolean value);
    
}
