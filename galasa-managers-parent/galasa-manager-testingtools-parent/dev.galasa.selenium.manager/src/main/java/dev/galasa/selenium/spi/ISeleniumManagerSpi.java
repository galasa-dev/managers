/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium.spi;

import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebPage;

/**
 * Provides the SPI access to the Selenium Manager for other managers to provision WebPages
 */
public interface ISeleniumManagerSpi extends ISeleniumManager {

    public IWebPage allocateWebPage();

    public IWebPage allocateWebPage(String url);

    public IWebPage allocateWebPage(String url, String tag);

    public IWebPage getWebPage(String tag);

}