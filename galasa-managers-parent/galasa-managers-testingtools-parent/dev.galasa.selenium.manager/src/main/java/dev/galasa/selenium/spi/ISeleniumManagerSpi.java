/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.selenium.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IWebDriver;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.selenium.SeleniumManagerException;

public interface ISeleniumManagerSpi {
    
    @NotNull 
    IWebDriver provisionWebDriver(Browser browser) throws ResourceUnavailableException, SeleniumManagerException;
}