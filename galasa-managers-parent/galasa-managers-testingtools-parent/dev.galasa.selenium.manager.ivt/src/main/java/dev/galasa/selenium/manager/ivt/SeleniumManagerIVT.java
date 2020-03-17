package dev.galasa.selenium.manager.ivt;

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
    public void test() throws SeleniumManagerException {
        IWebPage page = seleniumManager.allocateWebPage("http://www.google.com", "A");
        logger.info(page.getPageSource());
    }
}