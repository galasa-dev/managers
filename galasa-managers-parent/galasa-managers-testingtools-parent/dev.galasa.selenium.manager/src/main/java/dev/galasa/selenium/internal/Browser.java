package dev.galasa.selenium.internal;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;

import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.internal.properties.SeleniumGeckoPath;
import dev.galasa.selenium.internal.properties.SeleniumWebDriver;

public enum Browser {
  FIREFOX, IE, CHROME, EDGE;

  final static Log logger = LogFactory.getLog(Browser.class);

  private static final String EDGE_VERSION = "v6.17134";
  private static final String GECKO_VERSION = "v0.17.0";
  private static final String IE_VERSION = "v3.9";
  private static final String IEDRIVERSERVER = "IEDriverServer-x32-";
  private static final String CHROME_VERSION = "v2.38";

  private static final String FIREFOX_PROFILE = "default2";
  private static final String MAC_OS_X = "mac";
  private static final String WINDOWS = "windows";
  private static final String WINDOWS_10 = "windows 10";
  private static final String LINUX = "linux";
  private static final String DRIVERS_DIR = "drivers/";
  private static final String EXE = ".exe";

  private static final String EDGE_DRIVER =
      DRIVERS_DIR + "MicrosoftWebDriver-" + EDGE_VERSION + EXE;

  private static final String IE_DRIVER = DRIVERS_DIR + IEDRIVERSERVER + IE_VERSION + EXE;

  private static final String CHROME_DRIVER = DRIVERS_DIR + "chromedriver-" + CHROME_VERSION + EXE;

  private static final String FIREFOX_BIN = "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe";
  private static final String FIREFOX_BIN_MAC =
      "/Applications/IBM Firefox.app/Contents/MacOS/firefox-bin";
  @SuppressWarnings("unused")
  private static final String EDGE_BIN =
      "C:\\Windows\\SystemApps\\Microsoft.MicrosoftEdge_8wekyb3d8bbwe\\MicrosoftEdge.exe";
  private static final String EDGE_PROPERTY = "webdriver.edge.driver";
  private static final String IE_PROPERTY = "webdriver.ie.driver";
  private static final String CHROME_PROPERTY = "webdriver.chrome.driver";

  public static WebDriver getWebDriver(String instance) throws IOException, SeleniumManagerException {
    try {
      String driver = SeleniumWebDriver.get(instance);
      switch (getBrowser(driver)) {
        case FIREFOX:
          return getGeckoProperty(instance);
        default:
          return null;
      }
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get driver instance", e);
    }
  }

  private static WebDriver getGeckoProperty(String instance) throws IOException, SeleniumManagerException {
    FirefoxOptions options = new FirefoxOptions();

    try {
      System.setProperty("webdriver.gecko.driver", SeleniumGeckoPath.get(instance));
    } catch (Exception e) {
      throw new SeleniumManagerException("Unable to get Gecko path from CPS for instance: " + instance, e);
    }

    ProfilesIni profile = new ProfilesIni();
    FirefoxProfile ffProfile = profile.getProfile("default2");

    if (ffProfile == null) {
      ffProfile = new FirefoxProfile();
    }

    // accept SSL certs
    FirefoxOptions capabilities = new FirefoxOptions();
    capabilities.setAcceptInsecureCerts(true);
    capabilities.setCapability("moz:firefoxOptions", options);
    capabilities.setProfile(ffProfile);

    return new FirefoxDriver(capabilities);
  }

  public static Browser getBrowser(String browser) {
    browser = browser.trim();

    for (Browser d : values()) {
      if (browser.equalsIgnoreCase(d.name())) {
        return d;
      }
    }
    return null;
  }
}
