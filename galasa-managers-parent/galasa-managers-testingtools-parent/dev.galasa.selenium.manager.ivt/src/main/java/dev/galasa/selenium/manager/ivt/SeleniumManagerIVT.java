package dev.galasa.selenium.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.openqa.selenium.Alert;

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
        assertThat(page.getTitle()).containsOnlyOnce("DuckDuckGo");
        assertThat(page.findElementById("search_form_input_homepage").getAttribute("value")).isEmpty();
        page.sendKeysToElementById("search_form_input_homepage", "galasa");
        assertThat(page.findElementById("search_form_input_homepage").getAttribute("value")).isEqualTo("galasa");
        page.clearElementById("search_form_input_homepage");
        assertThat(page.findElementById("search_form_input_homepage").getAttribute("value")).isEmpty();
    }

    @Test
    public void clickingFields() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage("https://duckduckgo.com", "B");
        assertThat(page.getTitle()).containsOnlyOnce("DuckDuckGo");
        page.clickElementByCssSelector("a.header__button--menu.js-side-menu-open");
        page.clickElementByLinkText("Twitter");
        assertThat(page.getTitle()).contains("DuckDuckGo (@DuckDuckGo)");
    }

    @Test
    public void navigateGalasaGithub() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage("https://duckduckgo.com", "C");
        logger.info("Page Title: " + page.getTitle());
        assertThat(page.getTitle()).containsOnlyOnce("DuckDuckGo");
        page.sendKeysToElementById("search_form_input_homepage", "galasa dev github");
        page.clickElementById("search_button_homepage");
        page.clickElementByLinkText("galasa · GitHub");
        page.clickElementByPartialLinkText("People");
        assertThat(page.findElementsByLinkText("rsomers1998")).isNotEmpty();
    }
}