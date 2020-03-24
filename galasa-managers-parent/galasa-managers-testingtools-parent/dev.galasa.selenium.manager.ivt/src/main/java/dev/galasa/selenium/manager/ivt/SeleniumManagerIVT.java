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

    @Test
    public void sendingKeysAndClearingFields() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage("https://duckduckgo.com", "A");
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce("DuckDuckGo");
        assertThat(page.findElementById("search_form_input_homepage").getAttribute("value")).isEmpty();
        page.sendKeysToElementById("search_form_input_homepage", "galasa");
        assertThat(page.findElementById("search_form_input_homepage").getAttribute("value")).isEqualTo("galasa");
        page.clearElementById("search_form_input_homepage");
        assertThat(page.findElementById("search_form_input_homepage").getAttribute("value")).isEmpty();
        page.quit();
    }

    @Test
    public void clickingFields() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage("https://duckduckgo.com", "B");
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce("DuckDuckGo");
        page.clickElementByCssSelector("a.header__button--menu.js-side-menu-open");
        page.clickElementByLinkText("Twitter");
        page.waitForElementByLinkText("duckduckgo.com");
        assertThat(page.getTitle()).contains("DuckDuckGo (@DuckDuckGo)");
        page.quit();
    }

    @Test
    public void navigateGalasaGithub() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage("https://duckduckgo.com", "C");
        page.maximize();
        assertThat(page.getTitle()).containsOnlyOnce("DuckDuckGo");
        page.sendKeysToElementById("search_form_input_homepage", "galasa dev github");
        page.clickElementById("search_button_homepage");
        page.clickElementByLinkText("galasa · GitHub");
        page.clickElementByPartialLinkText("People");
        page.waitForElementByLinkText("rsomers1998");
        assertThat(page.findElementsByLinkText("rsomers1998")).isNotEmpty();
        page.quit();
    }

}