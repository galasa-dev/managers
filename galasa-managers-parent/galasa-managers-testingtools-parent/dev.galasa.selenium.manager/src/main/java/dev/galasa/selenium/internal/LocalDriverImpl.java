package dev.galasa.selenium.internal;

import java.nio.file.Path;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;

import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IChromeOptions;
import dev.galasa.selenium.IEdgeOptions;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.IInternetExplorerOptions;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;

public class LocalDriverImpl extends DriverImpl implements ISeleniumManager{
    private Path screenshotRasDirectory;
    private Browser browser;

    public LocalDriverImpl(Browser browser, Path screenshotRasDirectory) throws SeleniumManagerException {
        this.screenshotRasDirectory = screenshotRasDirectory.resolve(browser.getDriverName());
        this.browser = browser;
    }

    @Override
    public IWebPage allocateWebPage() throws SeleniumManagerException {
        return allocateWebPage(null);
    }

    @Override
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException {

        WebDriver driver = null;

        try {
            driver = LocalBrowser.getWebDriver(this.browser);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IFirefoxOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getGeckoDriver(options);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, ChromeOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getChromeDriver(options);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, EdgeOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getEdgeDriver(options);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, InternetExplorerOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getIEDriver(options);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type:" + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, OperaOptions options) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            driver = LocalBrowser.getOperaDriver(options);

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type:" + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IFirefoxOptions getFirefoxOptions() {
        return new FirefoxOptionsImpl();
    }

    @Override
    public IChromeOptions getChromeOptions() {
        return new ChromeOptionsImpl();
    }

    @Override
    public IEdgeOptions getEdgeOptions() {
        return new EdgeOptionsImpl();
    }

    @Override
    public IInternetExplorerOptions getInternetExplorerOptions() {
        return new InternetExplorerOptionsImpl();
    }

    public void discard() {
       discardPages();
    }
}