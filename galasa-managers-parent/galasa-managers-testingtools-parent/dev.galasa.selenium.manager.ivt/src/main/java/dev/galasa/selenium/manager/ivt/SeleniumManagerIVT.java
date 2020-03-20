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
    public void navigateGalasaDev() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage("http://www.galasa.dev", "A");
        logger.info("Page Title: " + page.getTitle());
        assertThat(page.getTitle()).containsOnlyOnce("Home | Galasa");
        page.clickElementByPartialLinkText("docs");
        assertThat(page.getTitle()).containsOnlyOnce("Introduction | Galasa");
    }
}