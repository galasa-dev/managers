/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.Assert.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesNamespace;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManager;
import dev.galasa.selenium.SeleniumManagerException;

@Test
public class SeleniumManagerIVT {

    @Logger
    public Log logger;

    @SeleniumManager
    public ISeleniumManager SeleniumManager;
    
    @SeleniumManager(browser = Browser.FIREFOX)
    public ISeleniumManager ChromeSeleniumManager;
    
//    @KubernetesNamespace()
//    public IKubernetesNamespace thing;

    public static final String WEBSITE = "https://duckduckgo.com";
    public static final String TITLE = "DuckDuckGo";
    public static final String VALUE = "value";
    public static final String SEARCHID = "search_form_input_homepage";

    @Test
    public void sendingKeysAndClearingFields() throws SeleniumManagerException {
    	SeleniumManager.getEdgeOptions();
        IWebPage page = SeleniumManager.allocateWebPage(WEBSITE);
        page.takeScreenShot();
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        assertThat(page.findElementById(SEARCHID).getAttribute(VALUE)).isEmpty();
        page.sendKeysToElementById(SEARCHID, "galasa");
        assertThat(page.findElementById(SEARCHID).getAttribute(VALUE)).isEqualTo("galasa");
        page.clearElementById(SEARCHID);
        assertThat(page.findElementById(SEARCHID).getAttribute(VALUE)).isEmpty();
        page.quit();
    }

    @Test
    public void clickingFields() throws SeleniumManagerException {
        IWebPage page = ChromeSeleniumManager.allocateWebPage(WEBSITE);
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.clickElementByCssSelector("a.header__button--menu.js-side-menu-open")
            .clickElementByLinkText("Twitter").takeScreenShot()
            .waitForElementByLinkText("duckduckgo.com");
        assertThat(page.getTitle()).contains("DuckDuckGo (@DuckDuckGo)");
        page.quit();
    }

    @Test
    public void navigateGalasaGithub() throws SeleniumManagerException {
        IWebPage page = SeleniumManager.allocateWebPage(WEBSITE);
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.sendKeysToElementById(SEARCHID, "galasa dev github")
            .clickElementById("search_button_homepage")
            .clickElementByLinkText("galasa · GitHub")
            .clickElementByPartialLinkText("Repositories").takeScreenShot();
        assertThat(page.findElementsByLinkText("framework")).isNotEmpty();
        page.quit();
    }

    @Test
    public void testOptionsCanBeUsed() throws SeleniumManagerException {
        IFirefoxOptions options = SeleniumManager.getFirefoxOptions();
        options.setHeadless(true);
        IWebPage page = SeleniumManager.allocateWebPage(WEBSITE, options);
        page.maximize().takeScreenShot();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.quit();
    }

}