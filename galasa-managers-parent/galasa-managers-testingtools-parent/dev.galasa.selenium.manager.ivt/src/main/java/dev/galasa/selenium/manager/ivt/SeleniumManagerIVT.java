/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManager;
import dev.galasa.selenium.SeleniumManagerException;

@Test
public class SeleniumManagerIVT {

    @Logger
    public Log logger;

    @SeleniumManager
    public ISeleniumManager seleniumManager;

    public final String WEBSITE = "https://duckduckgo.com";
    public final String TITLE = "DuckDuckGo";
    public final String VALUE = "value";
    public final String SEARCHID = "search_form_input_homepage";

    @Test
    public void sendingKeysAndClearingFields() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage(WEBSITE);
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
        IWebPage page = seleniumManager.allocateWebPage(WEBSITE);
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.clickElementByCssSelector("a.header__button--menu.js-side-menu-open")
            .clickElementByLinkText("Twitter")
            .waitForElementByLinkText("duckduckgo.com");
        assertThat(page.getTitle()).contains("DuckDuckGo (@DuckDuckGo)");
        page.quit();
    }

    @Test
    public void navigateGalasaGithub() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage(WEBSITE);
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce(TITLE);
        page.sendKeysToElementById(SEARCHID, "galasa dev github")
            .clickElementById("search_button_homepage")
            .clickElementByLinkText("galasa · GitHub")
            .clickElementByPartialLinkText("People");
        assertThat(page.findElementsByLinkText("rsomers1998")).isNotEmpty();
        page.quit();
    }

}